#!/usr/bin/env bash
set -euo pipefail

echo "▶ Starting Campus Marketplace…"
cd "$(dirname "$0")"

# 0) Load .env if present (safe if missing)
if [[ -f .env ]]; then
  echo "ℹ Loading .env"
  set -a
  source .env
  set +a
fi

# 1) Sanity checks
command -v java >/dev/null || { echo "❌ Java not found (need 17+)."; exit 1; }
MVN_CMD="./mvnw"
[[ -x "$MVN_CMD" ]] || MVN_CMD="mvn"
command -v "$MVN_CMD" >/dev/null || { echo "❌ Maven not found."; exit 1; }

# 2) Defaults (can be overridden via .env)
SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://commitstorm.c3k8w4gacaeh.us-west-2.rds.amazonaws.com:3306/campusMarket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true}"
SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-admin}"
SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-commitstorm}"
SERVER_PORT="${SERVER_PORT:-8080}"

# 3) Optionally bring up DB with Docker Compose (set START_DB=true to enable local DB)
START_DB="${START_DB:-false}"
if [[ "${START_DB}" == "true" ]]; then
  if command -v docker >/dev/null && command -v docker compose >/dev/null; then
    echo "🐋 Ensuring MySQL & Adminer are up (docker compose up -d)…"
    docker compose up -d
  else
    echo "⚠ Docker not found; skipping docker compose."
  fi
fi

# 4) Wait for DB port to accept connections (host & port derived from JDBC URL)
#    Works for localhost:3306 or RDS endpoints
host_port="$(echo "$SPRING_DATASOURCE_URL" | sed -E 's#^jdbc:mysql://([^/?]+).*#\1#')"
host="${host_port%%:*}"
port="${host_port##*:}"
echo "⏳ Checking connection to ${host}:${port} …"
for i in {1..30}; do
  (command -v nc >/dev/null && nc -z "$host" "$port") || (printf "" >/dev/tcp/"$host"/"$port" 2>/dev/null) && { echo "✅ Database is reachable."; break; }
  sleep 2
  if [[ $i -eq 30 ]]; then echo "❌ Could not reach database at ${host}:${port}"; exit 1; fi
done

# 5) Build (skip tests for speed) and run with env vars
echo "🧱 Building (skip tests)…"
$MVN_CMD -DskipTests clean package

echo "🚀 Running app on port ${SERVER_PORT} …"
SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
SERVER_PORT="$SERVER_PORT" \
$MVN_CMD spring-boot:run

echo "✅ Application started."
echo "📚 Swagger: http://localhost:${SERVER_PORT}/swagger-ui.html"
echo "❤️ Health:  http://localhost:${SERVER_PORT}/health"
