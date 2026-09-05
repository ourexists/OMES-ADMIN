#!/usr/bin/env bash
# OMES Docker helper — usage: ./docker/omes.sh <build|up|down|logs|ps> [args]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/docker/.env"
ENV_EXAMPLE="$ROOT/docker/.env.example"
COMPOSE_FILE="$ROOT/docker-compose.yml"

ensure_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    echo "Created docker/.env from example."
  fi
}

compose() {
  ensure_env
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

cmd="${1:-}"
shift || true

case "$cmd" in
  build)
    ensure_env
    [[ -f "$ROOT/config/config.properties" ]] || { echo "Missing config/config.properties"; exit 1; }
    [[ -f "$ROOT/config/era-token.yml" ]] || { echo "Missing config/era-token.yml"; exit 1; }
    if [[ "${SKIP_MVN:-0}" != "1" ]]; then
      echo "==> mvn package (admin + sas)"
      mvn -pl omes-runner-admin,omes-runner-sas -am clean package -DskipTests
    fi
    admin_jar=$(ls -1 omes-runner-admin/target/omes-runner-admin-*.jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)
    sas_jar=$(ls -1 omes-runner-sas/target/omes-runner-sas-*.jar 2>/dev/null | grep -v '\.original$' | head -n1 || true)
    [[ -n "$admin_jar" && -n "$sas_jar" ]] || { echo "Fat JARs missing. Unset SKIP_MVN=1 and rebuild."; exit 1; }
    echo "==> docker build"
    compose --profile app build
    echo "OK. Next: ./docker/omes.sh up"
    ;;
  up)
    target="${1:-all}"
    case "$target" in
      infra) compose up -d postgres redis rabbitmq ;;
      app|all) compose --profile app up -d ;;
      *) echo "up target: infra | all"; exit 1 ;;
    esac
    compose ps
    echo "Web http://127.0.0.1:8080 | SAS http://127.0.0.1:9400 | Admin :10010 | RabbitMQ :15672"
    ;;
  down)
    target="${1:-all}"
    case "$target" in
      app)
        compose --profile app stop admin sas web
        compose --profile app rm -f admin sas web
        ;;
      infra)
        compose stop postgres redis rabbitmq
        compose rm -f postgres redis rabbitmq
        ;;
      all) compose --profile app down ;;
      *) echo "down target: app | infra | all"; exit 1 ;;
    esac
    echo "Stopped ($target)."
    ;;
  logs)
    compose --profile app logs -f --tail=200 "$@"
    ;;
  ps)
    compose --profile app ps
    ;;
  *)
    echo "Usage: $0 <build|up|down|logs|ps> [args]"
    echo "  build              Maven package + image build (SKIP_MVN=1 to skip Maven)"
    echo "  up [infra|all]     Start stack (default all)"
    echo "  down [app|infra|all]"
    echo "  logs [service...]"
    echo "  ps"
    exit 1
    ;;
esac
