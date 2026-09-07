#!/usr/bin/env bash
# OMES Docker — usage: ./docker.sh <build|up|down> [infra]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
WEB_ROOT="$(cd "$ROOT/.." && pwd)/omes-iomp-web-admin"

[[ -f .env ]] || { cp .env.example .env; echo "Created .env from .env.example"; }

compose() { docker compose "$@"; }

cmd="${1:-}"
shift || true

case "$cmd" in
  build)
    [[ -f config/config.properties && -f config/era-token.yml ]] || { echo "Missing config/*"; exit 1; }
    if [[ "${SKIP_MVN:-0}" != "1" ]]; then
      echo "==> mvn package"
      mvn -pl omes-iomp-runner-admin,omes-iomp-runner-sas -am clean package -DskipTests
    fi
    ls omes-iomp-runner-admin/target/omes-iomp-runner-admin-*.jar >/dev/null 2>&1
    ls omes-iomp-runner-sas/target/omes-iomp-runner-sas-*.jar >/dev/null 2>&1

    if [[ "${SKIP_WEB:-0}" != "1" ]]; then
      [[ -d "$WEB_ROOT" ]] || { echo "Missing $WEB_ROOT"; exit 1; }
      echo "==> vite build → dist"
      ( cd "$WEB_ROOT"
        [[ -d node_modules ]] || npm install --no-audit --no-fund
        VITE_SAS_BASE_URL= npx vite build
      )
    fi
    [[ -f "$WEB_ROOT/dist/index.html" ]] || { echo "Missing frontend dist"; exit 1; }

    echo "==> docker compose build"
    compose --profile app build
    echo "OK → ./docker.sh up"
    ;;
  up)
    target="${1:-all}"
    if [[ "$target" == "infra" ]]; then
      compose up -d postgres redis rabbitmq
    else
      compose --profile app up -d
    fi
    compose ps
    echo "Web :8080 | SAS :9400 | Admin :10010 | RabbitMQ mgmt :15672"
    ;;
  down)
    compose --profile app down
    echo "Stopped."
    ;;
  *)
    echo "Usage: $0 <build|up|down> [infra]"
    echo "  build   SKIP_MVN=1 / SKIP_WEB=1 to skip steps"
    exit 1
    ;;
esac
