#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUN_DIR="$ROOT_DIR/.local/run"

stop_pid_file() {
  local name="$1"
  local pid_file="$RUN_DIR/${name}.pid"
  if [ -f "$pid_file" ]; then
    local pid
    pid=$(cat "$pid_file")
    if kill -0 "$pid" 2>/dev/null; then
      echo "[停止] $name (PID $pid)"
      kill "$pid" 2>/dev/null || true
      for _ in $(seq 1 10); do
        kill -0 "$pid" 2>/dev/null || break
        sleep 1
      done
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  fi
}

echo "停止本地服务 ..."

for svc in frontend backend; do
  stop_pid_file "$svc"
done

for port in 3000 8080; do
  pids=$(ss -tlnp 2>/dev/null | grep ":${port} " | grep -oP 'pid=\K[0-9]+' || true)
  for pid in $pids; do
    echo "[清理] 端口 :$port PID $pid"
    kill "$pid" 2>/dev/null || true
  done
done

echo "完成。"
