#!/usr/bin/env bash
# PostgreSQL 헬퍼.

set -euo pipefail

readonly PSQL_HOST="${PSQL_HOST:-localhost}"
readonly PSQL_PORT="${PSQL_PORT:-5432}"
readonly PSQL_DB="${PSQL_DB:-esgt3}"
readonly PSQL_USER="${PSQL_USER:-esgt3}"

psql_query() {
    PGPASSWORD="${PSQL_PASSWORD:-esgt3-local}" \
    psql -h "$PSQL_HOST" -p "$PSQL_PORT" -U "$PSQL_USER" -d "$PSQL_DB" \
        --no-psqlrc --quiet --tuples-only -A -F$'\t' -c "$1"
}

psql_query_safe() {
    # 파라미터 바인딩 — SQL Injection 방지
    local sql="$1"; shift
    PGPASSWORD="${PSQL_PASSWORD:-esgt3-local}" \
    psql -h "$PSQL_HOST" -p "$PSQL_PORT" -U "$PSQL_USER" -d "$PSQL_DB" \
        --no-psqlrc --quiet --tuples-only -A -F$'\t' \
        -v ON_ERROR_STOP=on "$@" -c "$sql"
}
