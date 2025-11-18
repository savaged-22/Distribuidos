#!/usr/bin/env bash
set -euo pipefail

# ---------- Config ----------
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/pids"
OPS_FILE="/tmp/ops.jsonl"              # cambia si quieres otro archivo
PS_CONCURRENCY=2                       # hilos por PS
PS_COUNT=0                             # cuántos PS lanzar (0 = ninguno)
WITH_SEDE_B=0                          # 1 para levantar también sedeB
BUILD_FIRST=1                          # 0 para saltar build
GRADLEW="$ROOT_DIR/gradlew"
# --------------------------------

usage() {
  cat <<EOF
Uso: $0 [opciones]
  --no-build            No ejecutar 'gradlew clean build -x test'
  --with-b              Levantar también sedeB (GA_B, Actors_B, GC_B)
  --ps N                Lanzar N procesos PS (perfil sedeA por defecto)
  --ps-concurrency N    Hilos por PS (default $PS_CONCURRENCY)
  --ops FILE            Ruta de archivo de operaciones (default $OPS_FILE)
  --help                Mostrar ayuda
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build) BUILD_FIRST=0; shift ;;
    --with-b) WITH_SEDE_B=1; shift ;;
    --ps) PS_COUNT="${2:-0}"; shift 2 ;;
    --ps-concurrency) PS_CONCURRENCY="${2:-2}"; shift 2 ;;
    --ops) OPS_FILE="${2:-$OPS_FILE}"; shift 2 ;;
    --help) usage; exit 0 ;;
    *) echo "Opción desconocida: $1"; usage; exit 1 ;;
  esac
done

mkdir -p "$LOG_DIR" "$PID_DIR"

require_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "Falta comando: $1"; exit 1; }; }
require_cmd "$GRADLEW"

# 1) Build
if [[ $BUILD_FIRST -eq 1 ]]; then
  echo "[build] Compilando…"
  "$GRADLEW" clean build -x test
fi

# 2) Archivo de operaciones (si no existe)
if [[ ! -s "$OPS_FILE" ]]; then
  cat > "$OPS_FILE" <<'EOF'
{"type":"DEVOLUCION","usuarioId":"u1","libroId":"L-001","idempotencyKey":"k-001"}
{"type":"RENOVACION","usuarioId":"u1","libroId":"L-001","idempotencyKey":"k-002"}
{"type":"PRESTAMO","usuarioId":"u2","libroId":"L-002","idempotencyKey":"k-003"}
EOF
  echo "[ops] Creado $OPS_FILE"
fi

# 3) Funciones para lanzar servicios
start_service() {
  local module="$1"; shift
  local name="$1"; shift
  local args="$*"
  local log="$LOG_DIR/${name}.log"

  echo "[run] $module ($name) $args"
  nohup "$GRADLEW" "$module:bootRun" --args="$args" >"$log" 2>&1 &
  local pid=$!
  echo $pid > "$PID_DIR/${name}.pid"
  echo "  -> PID $pid | log: $log"
}

wait_for_log() {
  # espera a que aparezca un patrón en log (timeout 60s)
  local log="$1"; local pattern="$2"; local t=0
  until grep -qE "$pattern" "$log" 2>/dev/null; do
    sleep 1; t=$((t+1)); [[ $t -gt 60 ]] && { echo "timeout esperando $pattern en $log"; return 1; }
  done
}

# 4) Lanzar Sede A
start_service ":service-ga"     "ga_A"     "--spring.profiles.active=sedeA"
start_service ":service-actors" "actors_A" "--spring.profiles.active=sedeA"
start_service ":service-gc"     "gc_A"     "--spring.profiles.active=sedeA"

# Opcional: espera “Started” en logs para dar tiempo a levantar
wait_for_log "$LOG_DIR/ga_A.log"     "Started|Tomcat|Netty|JVM running|Started .* in"
wait_for_log "$LOG_DIR/actors_A.log" "Started|JVM running"
wait_for_log "$LOG_DIR/gc_A.log"     "Started|JVM running"

# 5) Lanzar Sede B si se pidió
if [[ $WITH_SEDE_B -eq 1 ]]; then
  start_service ":service-ga"     "ga_B"     "--spring.profiles.active=sedeB"
  start_service ":service-actors" "actors_B" "--spring.profiles.active=sedeB"
  start_service ":service-gc"     "gc_B"     "--spring.profiles.active=sedeB"

  wait_for_log "$LOG_DIR/ga_B.log"     "Started|JVM running"
  wait_for_log "$LOG_DIR/actors_B.log" "Started|JVM running"
  wait_for_log "$LOG_DIR/gc_B.log"     "Started|JVM running"
fi

# 6) Lanzar PS (N procesos, sedeA)
if [[ $PS_COUNT -gt 0 ]]; then
  for i in $(seq 1 "$PS_COUNT"); do
    local_log="$LOG_DIR/ps_A_$i.log"
    echo "[ps] Lanzando ps-client #$i (sedeA) ops=$OPS_FILE conc=$PS_CONCURRENCY"
    nohup "$GRADLEW" :ps-client:bootRun \
      --args="--spring.profiles.active=sedeA --ps.ops.file=$OPS_FILE --ps.concurrency=$PS_CONCURRENCY" \
      >"$local_log" 2>&1 &
    echo $! > "$PID_DIR/ps_A_$i.pid"
    echo "  -> PID $! | log: $local_log"
  done
fi

echo
echo "✅ Todo arriba. PIDs en $PID_DIR, logs en $LOG_DIR"
echo "Para apagar: ./stack-down.sh"
