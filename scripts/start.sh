#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "未检测到 Docker，请先运行: sudo scripts/install-docker.sh"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "未检测到 Docker Compose v2"
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  if getent group docker >/dev/null 2>&1 && ! id -nG "$USER" | grep -qw docker; then
    echo "当前用户不在 docker 组，无法访问 Docker。"
    echo "请执行以下命令后重试（或重新登录终端）："
    echo "  sudo usermod -aG docker $USER"
    echo "  newgrp docker"
    exit 1
  fi

  echo "Docker 守护进程未运行，尝试启动..."
  if command -v systemctl >/dev/null 2>&1; then
    sudo systemctl start docker || true
  fi
  if ! docker info >/dev/null 2>&1; then
    echo "无法连接 Docker，请确认 Docker 已启动且当前用户在 docker 组内。"
    exit 1
  fi
fi

echo "构建并启动全部服务（首次约 3~8 分钟）..."
docker compose up -d --build

echo ""
echo "等待核心服务就绪..."
for i in $(seq 1 60); do
  if docker compose ps --format json 2>/dev/null | grep -q '"Service":"postgres"' && \
    docker compose ps postgres 2>/dev/null | grep -q "(healthy)"; then
    break
  fi
  sleep 5
done

echo ""
docker compose ps
echo ""
echo "访问地址:"
echo "  前端:     http://localhost:3000"
echo "  后端:     http://localhost:8080"
echo "  API 文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "查看日志: docker compose logs -f"
echo "停止服务: docker compose down"
