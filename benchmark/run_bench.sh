#!/usr/bin/env bash
set -euo pipefail

# Настройки (можно переопределить через переменные окружения)
RPS_START=${RPS_START:-100}
RPS_END=${RPS_END:-1000}
RPS_STEP=${RPS_STEP:-100}
DURATION=${DURATION:-120} # сек
USE_DOCKER=${USE_DOCKER:-true}
DOCKER_IMAGE=${DOCKER_IMAGE:-denvazh/gatling:latest}
COMPOSE_FILE=${COMPOSE_FILE:-docker-compose.test.yml}
RESULTS_DIR=${RESULTS_DIR:-./gatling-results}
GATLING_USER_FILES_DIR=${GATLING_USER_FILES_DIR:-./gatling/user-files}
TARGET_HOST=${TARGET_HOST:-localhost}
TARGET_PORT=${TARGET_PORT:-8080}
TARGET_PATH=${TARGET_PATH:-/actuator/health}
# Список сервисов для тестирования (только gateway-service)
TARGET_SERVICES=${TARGET_SERVICES:-"gateway-service"}

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
  docker-compose -f "$COMPOSE_FILE" up -d
  echo "=== Run for RPS=${rps} ==="

  # Ждём, пока сервис станет доступным по HTTP
  echo "Waiting for target HTTP endpoints to be available on host..."
  timeout=60
  start=$(date +%s)
  
  # Для gateway-service проверяем порт 8080
  for svc in $TARGET_SERVICES; do
    port=8080  # gateway-service всегда на 8080
    echo "Checking http://localhost:${port}${TARGET_PATH} for service $svc"
    while true; do
      if curl --silent --max-time 2 -f "http://localhost:${port}${TARGET_PATH}" >/dev/null 2>&1; then
        break
      fi
      sleep 1
      if [ $(( $(date +%s) - start )) -ge $timeout ]; then
        echo "Service $svc did not become ready in $timeout seconds"
        exit 1
      fi
    done
  done

  # Запуск Gatling (Docker image)
  if [ "$USE_DOCKER" = "true" ]; then
    # Узнаём docker-compose сеть
    first_svc=$(echo $TARGET_SERVICES | awk '{print $1}')
    cid=$(docker-compose -f "$COMPOSE_FILE" ps -q "$first_svc" || true)
    if [ -n "$cid" ]; then
      network=$(docker inspect -f '{{range $k,$v := .NetworkSettings.Networks}}{{$k}}{{end}}' "$cid" 2>/dev/null || true)
    else
      network=""
    fi

    if [ -n "$network" ]; then
      echo "Detected docker-compose network: $network"
      DOCKER_NET_ARG=(--network "$network")
      FALLBACK_HOST=service
    else
      echo "Could not detect docker-compose network; will address services via host.docker.internal"
      DOCKER_NET_ARG=()
      FALLBACK_HOST=host.docker.internal
    fi

    # Запуск Gatling для каждого сервиса
    pids=()
    run_dirs=()
    run_svcs=()

    # record start timestamp for Prometheus queries
    START_TS=$(date -u +%s)
    
    for svc in $TARGET_SERVICES; do
      port=8080  # gateway-service всегда на 8080
      if [ "$FALLBACK_HOST" = "service" ]; then
        docker_target_host=$svc
      else
        docker_target_host=$FALLBACK_HOST
      fi

      timestamp=$(date +%Y%m%dT%H%M%S)
      run_dir="$RESULTS_DIR/${svc}-results-${rps}-${timestamp}"
      mkdir -p "$run_dir"
      run_dirs+=("$run_dir")
      run_svcs+=("$svc")

      echo "Starting Gatling for $svc -> host=${docker_target_host} port=${port}; results -> $run_dir"
      docker run --rm "${DOCKER_NET_ARG[@]}" \
        -v "$(pwd)/gatling/user-files":/opt/gatling/user-files \
        -v "$run_dir":/opt/gatling/results \
        -e "JAVA_OPTS=-DtargetRps=${rps} -DtargetHost=${docker_target_host} -DtargetPort=${port} -DtargetPath=${TARGET_PATH} -DdurationSec=${DURATION}" \
        "${DOCKER_IMAGE}" \
        -sf /opt/gatling/user-files -rf /opt/gatling/results -s simulations.GatewaySimulation &
      pids+=("$!")
    done

    # wait for all runs to finish and capture exit codes
    exit_code=0
    for pid in "${pids[@]}"; do
      wait "$pid" || exit_code=$?
    done
    if [ $exit_code -ne 0 ]; then
      echo "One or more Gatling runs failed (exit code $exit_code)"
    fi

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
  else
    if [ -z "${GATLING_HOME:-}" ]; then
      echo "GATLING_HOME not set. Set to local Gatling installation or set USE_DOCKER=true"
      exit 1
    fi
    "$GATLING_HOME"/bin/gatling.sh -sf ./gatling/user-files -rf ./gatling-results -s simulations.GatewaySimulation
  fi

  # Результаты каждого запуска сохраняются в папках внутри $RESULTS_DIR
  echo "Saved results for this step under directories matching: $RESULTS_DIR/*-results-${rps}-*"
  docker-compose -f "$COMPOSE_FILE" down
done

echo "All runs finished. Results are in $RESULTS_DIR"
