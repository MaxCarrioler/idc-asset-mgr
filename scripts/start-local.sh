#!/usr/bin/env bash
# 非 Docker 模式一键启动（PigX 生产级 RBAC + JDK 17）
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local/run"
LOG_DIR="$ROOT_DIR/.local/logs"
PG_HOST="${PG_HOST:-192.168.59.101}"
PG_PORT="${PG_PORT:-5432}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
NACOS_URL="${NACOS_URL:-http://localhost:8848/nacos/}"
REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"

mkdir -p "$RUN_DIR" "$LOG_DIR" \
  "$ROOT_DIR/logs/pig-upms-biz" \
  "$ROOT_DIR/logs/pig-auth" \
  "$ROOT_DIR/logs/pig-gateway" \
  "$ROOT_DIR/logs/pig-dc-server-biz" \
  "$ROOT_DIR/logs/pig-dc-accessory-biz"

if [ -x "$HOME/.local/opt/jdk-17/bin/java" ]; then
  export JAVA_HOME="$HOME/.local/opt/jdk-17"
elif [ -d /usr/lib/jvm/java-17-openjdk ]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
else
  echo "未找到 JDK 17，请先安装到 ~/.local/opt/jdk-17 或系统 java-17-openjdk"
  exit 1
fi

export PATH="$JAVA_HOME/bin:${HOME}/.local/bin:${PATH}"
export NACOS_SERVER_ADDR="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
export REDIS_HOST REDIS_PORT
export MYSQL_HOST MYSQL_PORT
export MYSQL_USER="${MYSQL_USER:-root}"
export MYSQL_PWD="${MYSQL_PWD:-root}"
export MYSQL_DB="${MYSQL_DB:-pig}"

cd "$ROOT_DIR"

is_running() {
  local port="$1"
  ss -tlnp 2>/dev/null | grep -q ":${port} "
}

wait_port() {
  local port="$1"
  local name="$2"
  local timeout="${3:-120}"
  for _ in $(seq 1 "$timeout"); do
    if is_running "$port"; then
      echo "  ✓ $name (:$port) 已就绪"
      return 0
    fi
    sleep 1
  done
  echo "  ✗ $name (:$port) 启动超时，查看日志: $LOG_DIR/${name}.log"
  return 1
}

start_maven_service() {
  local name="$1"
  local module="$2"
  local port="$3"
  local config_file="${4:-}"

  if is_running "$port"; then
    echo "[跳过] $name 已在运行 (:$port)"
    return 0
  fi

  echo "[启动] $name ..."
  local jar="$ROOT_DIR/$module/target/$(basename "$module").jar"
  local config_arg=""
  if [ -n "$config_file" ]; then
    config_arg="--spring.config.additional-location=file:${config_file}"
  fi
  (
    cd "$ROOT_DIR"
    if [ -f "$jar" ]; then
      # shellcheck disable=SC2086
      nohup java -jar "$jar" $config_arg >"$LOG_DIR/${name}.log" 2>&1 &
    else
      # shellcheck disable=SC2086
      nohup mvn spring-boot:run -pl "$module" -am \
        -Dspring-boot.run.arguments="$config_arg" \
        >"$LOG_DIR/${name}.log" 2>&1 &
    fi
    echo $! >"$RUN_DIR/${name}.pid"
  )
  wait_port "$port" "$name"
}

echo "========================================"
echo " IDC 管理系统 - PigX 本地一键启动"
echo "========================================"
echo "JAVA_HOME=$JAVA_HOME"
java -version 2>&1 | head -1
echo ""

echo "[检查] PostgreSQL ($PG_HOST:$PG_PORT) ..."
if ! pg_isready -h "$PG_HOST" -p "$PG_PORT" >/dev/null 2>&1; then
  echo "PostgreSQL 未就绪，请先启动数据库"
  exit 1
fi
echo "  ✓ PostgreSQL 正常"

echo "[检查] MySQL ($MYSQL_HOST:$MYSQL_PORT/$MYSQL_DB) ..."
ensure_mysql() {
  if (echo >"/dev/tcp/${MYSQL_HOST}/${MYSQL_PORT}") >/dev/null 2>&1; then
    return 0
  fi

  local mysql_env="$ROOT_DIR/.local/mysql-env"
  local data_dir="$ROOT_DIR/.local/mysql-data"
  local mysqld_bin="$mysql_env/bin/mysqld"
  if [ ! -x "$mysqld_bin" ]; then
    return 1
  fi

  echo "[启动] 本地 MySQL ($MYSQL_HOST:$MYSQL_PORT) ..."
  mkdir -p "$data_dir" "$LOG_DIR" "$RUN_DIR"
  if [ ! -d "$data_dir/mysql" ]; then
    "$mysqld_bin" --initialize-insecure --datadir="$data_dir" --basedir="$mysql_env" --user="$(id -un)"
  fi
  nohup "$mysqld_bin" \
    --datadir="$data_dir" \
    --basedir="$mysql_env" \
    --port="$MYSQL_PORT" \
    --bind-address=127.0.0.1 \
    --socket="$data_dir/mysql.sock" \
    --pid-file="$RUN_DIR/mysqld.pid" \
    --log-error="$LOG_DIR/mysqld.log" \
    >"$LOG_DIR/mysqld-stdout.log" 2>&1 &
  echo $! >"$RUN_DIR/mysqld-launcher.pid"
  for _ in $(seq 1 60); do
    if (echo >"/dev/tcp/${MYSQL_HOST}/${MYSQL_PORT}") >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  return 1
}

if ! ensure_mysql; then
  echo "MySQL 未就绪。可任选其一："
  echo "  1) 系统安装 MySQL/MariaDB 并导入 db-pig/pig.sql + docker/mysql-init/*.sql"
  echo "  2) 使用项目自带用户态 MySQL：.local/mysql-env（由 micromamba 安装）"
  exit 1
fi
echo "  ✓ MySQL 端口可达"

echo "[检查] Nacos ..."
if ! curl -sf --connect-timeout 3 "$NACOS_URL" >/dev/null; then
  echo "Nacos 未就绪，请先启动 Nacos（standalone）"
  exit 1
fi
echo "  ✓ Nacos 正常"

ensure_redis() {
  if (echo >"/dev/tcp/${REDIS_HOST}/${REDIS_PORT}") >/dev/null 2>&1; then
    return 0
  fi

  local redis_bin="${HOME}/.local/opt/redis/bin/redis-server"
  if [ -x "$redis_bin" ]; then
    echo "[启动] 本地 Redis ($REDIS_HOST:$REDIS_PORT) ..."
    mkdir -p "$ROOT_DIR/.local/redis"
    nohup "$redis_bin" --port "$REDIS_PORT" --bind "$REDIS_HOST" \
      --dir "$ROOT_DIR/.local/redis" --daemonize yes \
      --pidfile "$RUN_DIR/redis.pid" \
      >"$LOG_DIR/redis.log" 2>&1 || true
    sleep 1
    if (echo >"/dev/tcp/${REDIS_HOST}/${REDIS_PORT}") >/dev/null 2>&1; then
      return 0
    fi
  fi

  echo "Redis 未就绪，PigX 认证/网关依赖 Redis。"
  return 1
}

echo "[检查] Redis ($REDIS_HOST:$REDIS_PORT) ..."
if ! ensure_redis; then
  exit 1
fi
echo "  ✓ Redis 正常"

if [ ! -f "$ROOT_DIR/pig-dc/pig-dc-accessory-biz/target/pig-dc-accessory-biz.jar" ] \
  || [ ! -f "$ROOT_DIR/pig-auth/target/pig-auth.jar" ] \
  || [ ! -f "$ROOT_DIR/pig-upms/pig-upms-biz/target/pig-upms-biz.jar" ]; then
  echo "[编译] mvn install -pl pig-auth,pig-upms/pig-upms-biz,pig-dc/pig-dc-server-biz,pig-dc/pig-dc-accessory-biz,pig-gateway -am ..."
  mvn install -DskipTests \
    -pl pig-auth,pig-upms/pig-upms-biz,pig-dc/pig-dc-server-biz,pig-dc/pig-dc-accessory-biz,pig-gateway -am
fi

if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo "[安装] frontend npm install ..."
  (cd "$ROOT_DIR/frontend" && npm install)
fi

echo ""
echo "[启动] PigX 认证与微服务 ..."
start_maven_service "pig-upms-biz" "pig-upms/pig-upms-biz" 4000 "$ROOT_DIR/config/upms-local.yml"
start_maven_service "pig-auth" "pig-auth" 3001 "$ROOT_DIR/config/auth-local.yml"
start_maven_service "pig-dc-server-biz" "pig-dc/pig-dc-server-biz" 8081
start_maven_service "pig-dc-accessory-biz" "pig-dc/pig-dc-accessory-biz" 8082

GATEWAY_CONFIG="file:${ROOT_DIR}/config/gateway-local.yml"
if is_running 9999; then
  echo "[跳过] pig-gateway 已在运行 (:9999)"
else
  echo "[启动] pig-gateway ..."
  gateway_jar="$ROOT_DIR/pig-gateway/target/pig-gateway.jar"
  (
    cd "$ROOT_DIR"
    if [ -f "$gateway_jar" ]; then
      nohup java -jar "$gateway_jar" \
        --spring.config.additional-location="${GATEWAY_CONFIG}" \
        >"$LOG_DIR/pig-gateway.log" 2>&1 &
    else
      nohup mvn spring-boot:run -pl pig-gateway -am \
        -Dspring-boot.run.arguments="--spring.config.additional-location=${GATEWAY_CONFIG}" \
        >"$LOG_DIR/pig-gateway.log" 2>&1 &
    fi
    echo $! >"$RUN_DIR/pig-gateway.pid"
  )
  wait_port 9999 "pig-gateway"
fi

if is_running 3000; then
  echo "[跳过] frontend 已在运行 (:3000)"
else
  echo "[启动] frontend ..."
  (
    cd "$ROOT_DIR/frontend"
    nohup npm run dev >"$LOG_DIR/frontend.log" 2>&1 &
    echo $! >"$RUN_DIR/frontend.pid"
  )
  wait_port 3000 "frontend" 30
fi

echo ""
echo "========================================"
echo " 全部服务已启动（PigX 生产级 RBAC）"
echo "========================================"
echo "  前端:      http://localhost:3000"
echo "  网关:      http://localhost:9999"
echo "  认证:      http://localhost:3001 (经网关 /auth)"
echo "  用户权限:  http://localhost:4000 (经网关 /admin)"
echo "  Nacos:     http://localhost:8848/nacos"
echo ""
echo "  演示账号（密码均为 123456）:"
echo "    admin / accessory_admin / user"
echo ""
echo "  日志: $LOG_DIR"
echo "  停止: scripts/stop-local.sh"
echo "========================================"
