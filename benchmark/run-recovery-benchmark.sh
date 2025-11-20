#!/usr/bin/env bash
set -euo pipefail

# Настройки (можно переопределить через переменные окружения)
RPS_START=${RPS_START:-650}
RPS_END=${RPS_END:-650}
RPS_STEP=${RPS_STEP:-100}
DURATION=${DURATION:-100} # сек
USE_DOCKER=${USE_DOCKER:-true}
DOCKER_IMAGE=${DOCKER_IMAGE:-denvazh/gatling:latest}
COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.test.yml}
RESULTS_DIR=${RESULTS_DIR:-./gatling-recovery-results}
GATLING_USER_FILES_DIR=${GATLING_USER_FILES_DIR:-./gatling/user-files}

# Список сервисов для тестирования
TARGET_SERVICES=${TARGET_SERVICES:-"auth-service-common auth-service-reactive"}
SERVICE_PORTS=${SERVICE_PORTS:-"auth-service-common:8081 auth-service-reactive:8082"}

# Monitoring config
MONITOR=${MONITOR:-true}
MONITOR_COMPOSE=${MONITOR_COMPOSE:-./docker-compose.monitor.yml}
PROM_URL=${PROM_URL:-http://localhost:9090}
PROM_STEP=${PROM_STEP:-15}
PROM_OUT_DIR=${PROM_OUT_DIR:-./analysis/prometheus}
mkdir -p "$PROM_OUT_DIR"

# helper: wait for Prometheus ready endpoint
wait_prometheus_ready() {
  echo "Waiting for Prometheus to be ready at ${PROM_URL}/-/ready..."
  WAIT_SECS=60
  INTERVAL=2
  elapsed=0
  until curl -sS -f ${PROM_URL}/-/ready > /dev/null 2>&1; do
    if [ "$elapsed" -ge "$WAIT_SECS" ]; then
      echo "Prometheus did not become ready within ${WAIT_SECS}s. Showing Prometheus logs for debugging:"
      docker compose -f "$MONITOR_COMPOSE" logs --no-color prometheus | sed -n '1,200p'
      return 1
    fi
    printf "."
    sleep $INTERVAL
    elapsed=$((elapsed + INTERVAL))
  done
  echo "\nPrometheus ready"
  return 0
}

# helper: collect cpu/mem metrics for a run interval into PROM_OUT_DIR
collect_metrics_for_run() {
  local svc="$1"
  local start_ts="$2"
  local end_ts="$3"
  local out_prefix="$4"

  echo "Collecting Prometheus metrics for service $svc (start=$start_ts end=$end_ts)"
  
  # detect whether Prometheus has the compose service label
  label_values=$(curl -sS "${PROM_URL}/api/v1/label/container_label_com_docker_compose_service/values" || echo "")
  if echo "$label_values" | grep -q "\"$svc\""; then
    cpu_query="sum by (container_label_com_docker_compose_service) (rate(container_cpu_usage_seconds_total{container_label_com_docker_compose_service=\"$svc\"}[1m]))"
    mem_query="sum by (container_label_com_docker_compose_service) (container_memory_usage_bytes{container_label_com_docker_compose_service=\"$svc\"})"
  else
    echo "Label container_label_com_docker_compose_service not present for '$svc' — falling back to container name pattern match"
    cpu_query="sum by (container) (rate(container_cpu_usage_seconds_total{container=~\".*${svc}.*\"}[1m]))"
    mem_query="sum by (container) (container_memory_usage_bytes{container=~\".*${svc}.*\"})"
  fi

  # adapt step and rate window to the run duration so we get multiple samples
  run_dur=$((end_ts - start_ts))
  if [ $run_dur -le 0 ]; then
    run_dur=1
  fi
  # choose step ~ run_dur/10 but at least 1s
  step=$(( run_dur / 10 ))
  if [ $step -lt 1 ]; then
    step=1
  fi
  # choose rate window: prefer 1m for long runs, 30s for medium, 10s for short
  if [ $run_dur -ge 120 ]; then
    rate_window="1m"
  elif [ $run_dur -ge 30 ]; then
    rate_window="30s"
  else
    rate_window="10s"
  fi
  # substitute rate window into cpu_query if it contains [..]
  cpu_query_eval=$(echo "$cpu_query" | sed "s/\[.*\]/\[${rate_window}\]/")

  # attempt to compute CPU percent: try to get machine_cpu_cores from Prometheus
  cores=$(curl -sS "${PROM_URL}/api/v1/query?query=machine_cpu_cores" | python3 -c "import sys,json
data=json.load(sys.stdin)
res=data.get('data',{}).get('result',[])
print(res[0]['value'][1] if res else '0')" 2>/dev/null || echo 0)

  cpu_percent_query="(${cpu_query_eval}) * 100"
  if [ -n "$cores" ] && [ "$cores" != "0" ]; then
    # divide by machine cores to get percent of whole machine
    cpu_percent_query="(${cpu_query_eval}) * 100 / ${cores}"
  else
    echo "Warning: machine_cpu_cores not found; using rate()*100 (percent of single core)"
  fi

  cpu_url="${PROM_URL}/api/v1/query_range?query=$(python3 -c 'import urllib.parse,sys;print(urllib.parse.quote(sys.argv[1]))' "$cpu_percent_query")&start=${start_ts}&end=${end_ts}&step=${step}"
  mem_url="${PROM_URL}/api/v1/query_range?query=$(python3 -c 'import urllib.parse,sys;print(urllib.parse.quote(sys.argv[1]))' "$mem_query")&start=${start_ts}&end=${end_ts}&step=${step}"

  # retries
  for i in 1 2 3; do
    if curl -sS "$cpu_url" -o "${out_prefix}_${svc}_cpu.json"; then
      break
    fi
    echo "Retrying cpu query for $svc ($i)"
    sleep 1
  done
  for i in 1 2 3; do
    if curl -sS "$mem_url" -o "${out_prefix}_${svc}_mem.json"; then
      break
    fi
    echo "Retrying mem query for $svc ($i)"
    sleep 1
  done
  echo "Saved ${out_prefix}_${svc}_cpu.json and ${out_prefix}_${svc}_mem.json"
}

# helper: get port for service
get_port_for_service() {
  local svc="$1"
  for service_port in $SERVICE_PORTS; do
    local service=$(echo "$service_port" | cut -d':' -f1)
    local port=$(echo "$service_port" | cut -d':' -f2)
    if [ "$service" = "$svc" ]; then
      echo "$port"
      return 0
    fi
  done
  echo "8080" # default port
}

# helper: wait for service to be ready
wait_for_service() {
  local svc="$1"
  local port="$2"
  local path="$3"
  
  echo "Waiting for service $svc at http://localhost:${port}${path}"
  timeout=60
  start=$(date +%s)
  while true; do
    if curl --silent --max-time 2 -f "http://localhost:${port}${path}" >/dev/null 2>&1; then
      echo "Service $svc is ready"
      break
    fi
    sleep 1
    if [ $(( $(date +%s) - start )) -ge $timeout ]; then
      echo "Service $svc did not become ready in $timeout seconds"
      return 1
    fi
  done
  return 0
}

# helper: wait for PostgreSQL to be ready
wait_for_postgres() {
  echo "Waiting for PostgreSQL to be ready..."
  local timeout=30
  local start=$(date +%s)
  
  while true; do
    if docker-compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U test_user -d test_db >/dev/null 2>&1; then
      echo "PostgreSQL is ready"
      break
    fi
    sleep 2
    if [ $(( $(date +%s) - start )) -ge $timeout ]; then
      echo "PostgreSQL did not become ready in $timeout seconds"
      return 1
    fi
  done
  return 0
}

# helper: cleanup PostgreSQL data and restart
cleanup_postgres() {
  echo "Cleaning up PostgreSQL data..."
  
  # Останавливаем и удаляем контейнеры с volume
  docker-compose -f "$COMPOSE_FILE" down -v
  
  # Удаляем volume вручную (на случай если down -v не сработал)
  docker volume rm -f $(docker volume ls -q | grep postgres_data) 2>/dev/null || true
  docker volume rm -f $(docker volume ls -q | grep $(basename $(pwd))_postgres_data) 2>/dev/null || true
  
  # Запускаем заново
  docker-compose -f "$COMPOSE_FILE" up -d postgres
  
  # Ждем готовности PostgreSQL
  if ! wait_for_postgres; then
    echo "Failed to start PostgreSQL after cleanup"
    return 1
  fi
  
  echo "PostgreSQL cleaned up and restarted successfully"
  return 0
}

# Функция для полной очистки и перезапуска приложения
restart_application_stack() {
  echo "Restarting application stack with clean PostgreSQL..."
  
  # Полная очистка
  docker-compose -f "$COMPOSE_FILE" down -v
  
  # Убедимся, что все volumes удалены
  docker volume prune -f 2>/dev/null || true
  
  # Запускаем заново
  docker-compose -f "$COMPOSE_FILE" up -d
  
  # Ждем PostgreSQL
  if ! wait_for_postgres; then
    echo "Warning: PostgreSQL startup check failed, but continuing..."
  fi
  
  # Ждем сервисы
  for svc in $TARGET_SERVICES; do
    port=$(get_port_for_service "$svc")
    if ! wait_for_service "$svc" "$port" "/api/v1/auth/health"; then
      echo "Warning: Service $svc health check failed, but continuing..."
    fi
  done
  
  echo "Application stack restarted with clean database"
}

# Start the application stack
echo "Starting application stack..."
restart_application_stack

# start monitoring once if requested
if [ "$MONITOR" = "true" ]; then
  echo "Starting monitoring stack..."
  docker compose -f "$MONITOR_COMPOSE" up -d
  if ! wait_prometheus_ready; then
    echo "Prometheus not ready; continuing without monitoring"
    MONITOR=false
  fi
fi

mkdir -p "$RESULTS_DIR"

for (( rps=RPS_START; rps<=RPS_END; rps+=RPS_STEP )); do
  echo -e "\033[31m=== Run for RPS=${rps} / ${RPS_END} ===\033[0m"

  # Перезапускаем приложение с чистой БД перед каждым тестом
  echo "Restarting application with clean database for RPS=$rps..."
  restart_application_stack

  # record start timestamp for Prometheus queries
  START_TS=$(date -u +%s)

  # Запуск Gatling для каждого сервиса в параллельных контейнерах
  pids=()
  run_dirs=()
  run_svcs=()

  for svc in $TARGET_SERVICES; do
    port=$(get_port_for_service "$svc")
    
    timestamp=$(date +%Y%m%dT%H%M%S)
    run_dir="$RESULTS_DIR/${svc}-results-${rps}-${timestamp}"
    mkdir -p "$run_dir"
    run_dirs+=("$run_dir")
    run_svcs+=("$svc")

    echo "Starting Gatling for $svc -> host=localhost port=${port}; results -> $run_dir"
    
    # Проверяем существование директорий
    if [ ! -d "$(pwd)/gatling/user-files" ]; then
        echo "ERROR: Directory $(pwd)/gatling/user-files does not exist"
        exit 1
    fi
    
    if [ ! -d "$run_dir" ]; then
        echo "ERROR: Directory $run_dir does not exist"
        exit 1
    fi

    if [ "$USE_DOCKER" = "true" ]; then
      # Run Gatling in Docker container
      docker run --rm \
        --network host \
        -v "$(pwd)/gatling/user-files:/opt/gatling/user-files:ro" \
        -v "$run_dir:/opt/gatling/results:rw" \
        -e "JAVA_OPTS=-DtargetRps=${rps} -DtargetHost=localhost -DtargetPort=${port} -DdurationSec=${DURATION} -DserviceName=${svc}" \
        denvazh/gatling:latest \
        -sf /opt/gatling/user-files -rf /opt/gatling/results -s simulations.AuthServiceRecoverySimulation
    else
      if [ -z "${GATLING_HOME:-}" ]; then
        echo "GATLING_HOME not set. Set to local Gatling installation or set USE_DOCKER=true"
        exit 1
      fi
      JAVA_OPTS="-DtargetRps=${rps} -DtargetHost=localhost -DtargetPort=${port} -DtargetPath=/ -DdurationSec=${DURATION} -DserviceName=${svc}" \
      "$GATLING_HOME"/bin/gatling.sh -sf ./gatling/user-files -rf "$run_dir" -s simulations.AuthServiceRecoverySimulation 
    fi
  done

  # record end timestamp and collect Prometheus metrics for this run
  END_TS=$(date -u +%s)
  if [ "$MONITOR" = "true" ]; then
    for idx in "${!run_dirs[@]}"; do
      svc=${run_svcs[$idx]}
      run_dir=${run_dirs[$idx]}
      run_base=$(basename "$run_dir")
      out_prefix="${PROM_OUT_DIR}/${run_base}"
      collect_metrics_for_run "$svc" "$START_TS" "$END_TS" "$out_prefix"
      # copy the JSON results into the run directory for convenience
      cp "${out_prefix}_${svc}_cpu.json" "$run_dir/" 2>/dev/null || true
      cp "${out_prefix}_${svc}_mem.json" "$run_dir/" 2>/dev/null || true
    done
  fi

  echo "Saved results for this step under directories matching: $RESULTS_DIR/*-results-${rps}-*"
  
  # Останавливаем приложение после каждого теста для очистки
  echo "Stopping application stack after RPS=$rps test..."
  docker-compose -f "$COMPOSE_FILE" down -v
  
  # Short pause between runs
  echo "Waiting before next test..."
  sleep 3
done

echo "All runs finished. Results are in $RESULTS_DIR"

# Final cleanup
echo "Performing final cleanup..."
if [ "$MONITOR" = "true" ]; then
  docker compose -f "$MONITOR_COMPOSE" down
fi

# Полная очистка volumes
docker-compose -f "$COMPOSE_FILE" down -v
docker volume prune -f 2>/dev/null || true

echo "Fixing permissions..."
sudo chown -R $(id -u):$(id -g) "$RESULTS_DIR" 2>/dev/null || true
sudo chmod -R 755 "$RESULTS_DIR" 2>/dev/null || true

echo "Benchmark completed!"

echo "Fixing permissions..."
sudo chown -R $(id -u):$(id -g) "$RESULTS_DIR"
sudo chmod -R 755 "$RESULTS_DIR"
