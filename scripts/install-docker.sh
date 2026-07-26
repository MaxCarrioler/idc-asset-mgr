#!/usr/bin/env bash
set -euo pipefail

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  echo "Docker 已安装: $(docker --version)"
  echo "Compose: $(docker compose version)"
  exit 0
fi

if [[ "$(id -u)" -ne 0 ]]; then
  echo "需要 root 权限安装 Docker，请执行:"
  echo "  sudo $0"
  exit 1
fi

if command -v dnf >/dev/null 2>&1; then
  echo "正在通过 dnf 安装 Docker..."
  dnf install -y docker docker-cli-compose
  systemctl enable --now docker
elif command -v apt-get >/dev/null 2>&1; then
  echo "正在通过 apt 安装 Docker..."
  apt-get update
  apt-get install -y docker.io docker-compose-v2
  systemctl enable --now docker
else
  echo "未识别的包管理器，请手动安装 Docker 24+ 与 Compose v2"
  exit 1
fi

if [[ -n "${SUDO_USER:-}" ]]; then
  usermod -aG docker "$SUDO_USER"
  echo "已将用户 $SUDO_USER 加入 docker 组，请重新登录后生效。"
fi

echo "安装完成: $(docker --version)"
docker compose version
