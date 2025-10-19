#!/bin/sh
set -e

HOST="${DB_HOST:-db_container}"
PORT="${DB_PORT:-3306}"

echo "Waiting for MySQL at ${HOST}:${PORT} ..."
for i in $(seq 1 60); do
  if nc -z "${HOST}" "${PORT}"; then
    echo "MySQL is up."
    break
  fi
  echo "  [$i/60] waiting..."
  sleep 2
done

exec java $JAVA_OPTS -jar /app/app.jar
