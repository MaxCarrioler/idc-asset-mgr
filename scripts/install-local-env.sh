#!/usr/bin/env bash
# 非 Docker 本地环境一键安装（用户目录，无需 root）
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL="$HOME/.local/opt"
BIN="$HOME/.local/bin"
PGDATA="$HOME/.local/share/idc-pgdata"
MAMBA_ROOT="$HOME/micromamba"

mkdir -p "$LOCAL" "$BIN" "$ROOT_DIR/.local" "$PGDATA"

export PATH="$BIN:${MAMBA_ROOT}/bin:${PATH}"

install_maven() {
  if [ -x "$LOCAL/maven/bin/mvn" ]; then return 0; fi
  echo "[1/3] 安装 Maven..."
  tmp=$(mktemp -d)
  curl -fL --retry 3 -o "$tmp/maven.tar.gz" \
    "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz"
  rm -rf "$LOCAL/maven"
  mkdir -p "$LOCAL/maven"
  tar -xzf "$tmp/maven.tar.gz" -C "$LOCAL/maven" --strip-components=1
  ln -sf "$LOCAL/maven/bin/mvn" "$BIN/mvn"
}

install_node() {
  if [ -x "$LOCAL/node/bin/node" ]; then return 0; fi
  echo "[2/3] 安装 Node.js..."
  tmp=$(mktemp -d)
  curl -fL --retry 3 -o "$tmp/node.tar.xz" \
    "https://npmmirror.com/mirrors/node/v20.19.0/node-v20.19.0-linux-x64.tar.xz"
  rm -rf "$LOCAL/node"
  mkdir -p "$LOCAL/node"
  tar -xJf "$tmp/node.tar.xz" -C "$LOCAL/node" --strip-components=1
  ln -sf "$LOCAL/node/bin/node" "$BIN/node"
  ln -sf "$LOCAL/node/bin/npm" "$BIN/npm"
  ln -sf "$LOCAL/node/bin/npx" "$BIN/npx"
}

install_postgresql() {
  if [ -x "$MAMBA_ROOT/bin/postgres" ]; then return 0; fi
  echo "[3/3] 安装 PostgreSQL（micromamba）..."
  if [ ! -x "$MAMBA_ROOT/bin/micromamba" ]; then
    tmp=$(mktemp -d)
    curl -fL --retry 3 -o "$tmp/micromamba.tar.bz2" \
      "https://micro.mamba.pm/api/micromamba/linux-64/latest"
    mkdir -p "$MAMBA_ROOT/bin"
    tar -xjf "$tmp/micromamba.tar.bz2" -C "$MAMBA_ROOT/bin" --strip-components=1 bin/micromamba
  fi
  MAMBA_ROOT_PREFIX="$MAMBA_ROOT" "$MAMBA_ROOT/bin/micromamba" install -y -p "$MAMBA_ROOT" postgresql -c conda-forge
}

init_postgresql() {
  export PATH="$MAMBA_ROOT/bin:$PATH"
  if [ ! -f "$PGDATA/PG_VERSION" ]; then
    echo "初始化 PostgreSQL 数据目录..."
    initdb -D "$PGDATA" -U "$USER" --encoding=UTF8 --locale=C
    cat >> "$PGDATA/postgresql.conf" <<EOF
listen_addresses = 'localhost'
port = 5432
EOF
  fi

  if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
    echo "启动 PostgreSQL..."
    pg_ctl -D "$PGDATA" -l "$HOME/.local/share/idc-pg.log" start
    sleep 2
  fi

  psql -h localhost -p 5432 -d postgres -tc "SELECT 1 FROM pg_roles WHERE rolname='dcasset'" | grep -q 1 || \
    psql -h localhost -p 5432 -d postgres -c "CREATE USER dcasset WITH PASSWORD 'dcasset123' CREATEDB;"
  psql -h localhost -p 5432 -d postgres -tc "SELECT 1 FROM pg_database WHERE datname='idc_manager'" | grep -q 1 || \
    psql -h localhost -p 5432 -d postgres -c "CREATE DATABASE idc_manager OWNER dcasset;"
}

install_maven
install_node
install_postgresql
init_postgresql

echo ""
echo "环境安装完成。"
echo "  Java:  $(java -version 2>&1 | head -1)"
echo "  Maven: $(mvn -version 2>&1 | head -1)"
echo "  Node:  $(node -v)"
echo "  PG:    $(postgres --version)"
echo ""
echo "下一步: scripts/start-local.sh"
