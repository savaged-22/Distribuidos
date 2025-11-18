#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_DIR="$ROOT_DIR/pids"

if [[ ! -d "$PID_DIR" ]]; then
  echo "No existe $PID_DIR. ¿Ya corriste stack-up.sh?"
  exit 0
fi

echo "[down] Terminando procesos…"
for f in "$PID_DIR"/*.pid 2>/dev/null; do
  [[ -e "$f" ]] || continue
  pid=$(cat "$f" || true)
  if [[ -n "${pid:-}" ]] && kill -0 "$pid" 2>/dev/null; then
    echo "  - killing $pid ($f)"
    kill "$pid" || true
    sleep 1
    kill -9 "$pid" 2>/dev/null || true
  fi
  rm -f "$f"
done

echo "✅ Listo. PIDs limpiados. (Logs quedan en ./logs/)"
