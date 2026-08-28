#!/usr/bin/env bash
# 非 Docker 模式一键启动（轻量单体：JDK 17 + PostgreSQL + 前端）
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local/run"
LOG_DIR="$ROOT_DIR/.local/logs"

# PostgreSQL 连接（可用环境变量覆盖）
export DB_HOST="${DB_HOST:-192.168.59.101}"
export DB_PORT="${DB_PORT:-5432}"
export DB_NAME="${DB_NAME:-idc_manager}"
export DB_USER="${DB_USER:-dcasset}"
export DB_PASSWORD="${DB_PASSWORD:-dcasset123}"

mkdir -p "$RUN_DIR" "$LOG_DIR"

if [ -x "$HOME/.local/opt/jdk-17/bin/java" ]; then
  export JAVA_HOME="$HOME/.local/opt/jdk-17"
elif [ -d /usr/lib/jvm/java-17-openjdk ]; then
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
else
  echo "未找到 JDK 17，请先安装到 ~/.local/opt/jdk-17 或系统 java-17-openjdk"
  exit 1
fi

export PATH="$JAVA_HOME/bin:${HOME}/.local/bin:${HOME}/.local/opt/maven/bin:${PATH}"

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

echo "========================================"
echo " IDC 管理系统 - 本地一键启动（单体版）"
echo "========================================"
echo "JAVA_HOME=$JAVA_HOME"
java -version 2>&1 | head -1
echo ""

echo "[检查] PostgreSQL ($DB_HOST:$DB_PORT) ..."
if ! pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1; then
  echo "PostgreSQL 未就绪，请先启动数据库"
  exit 1
fi
echo "  ✓ PostgreSQL 正常"

# 确保业务库存在（首次启动自动创建；表结构与种子数据由应用自动初始化）
if command -v psql >/dev/null 2>&1; then
  if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres \
      -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" 2>/dev/null | grep -q 1; then
    echo "[建库] 创建数据库 ${DB_NAME} ..."
    PGPASSWORD="$DB_PASSWORD" createdb -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME"
  fi
else
  echo "[提示] 未安装 psql，跳过建库检查（请确保 ${DB_NAME} 库已存在）"
fi

APP_JAR="$ROOT_DIR/idc-app/target/idc-app.jar"
if [ ! -f "$APP_JAR" ]; then
  echo "[编译] mvn package -DskipTests ..."
  mvn package -DskipTests -q
fi

if [ ! -d "$ROOT_DIR/frontend/node_modules" ]; then
  echo "[安装] frontend npm install ..."
  (cd "$ROOT_DIR/frontend" && npm install)
fi

echo ""
if is_running 8080; then
  echo "[跳过] backend 已在运行 (:8080)"
else
  echo "[启动] backend ..."
  (
    cd "$ROOT_DIR"
    nohup java -jar "$APP_JAR" >"$LOG_DIR/backend.log" 2>&1 &
    echo $! >"$RUN_DIR/backend.pid"
  )
  wait_port 8080 "backend"
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
echo " 全部服务已启动（轻量单体版）"
echo "========================================"
echo "  前端:  http://localhost:3000"
echo "  后端:  http://localhost:8080"
echo "  API 文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "  演示账号（密码均为 123456）:"
echo "    admin / accessory_admin / user"
echo ""
echo "  日志: $LOG_DIR"
echo "  停止: scripts/stop-local.sh"
echo "========================================"
