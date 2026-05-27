#!/usr/bin/env bash
# Runbook 공통 함수 라이브러리.
# 사용: source "$(dirname "$0")/lib/common.sh"

set -euo pipefail

readonly ESG_T3_LOG_DIR="${ESG_T3_LOG_DIR:-./logs/runbook}"
mkdir -p "$ESG_T3_LOG_DIR"

log_info()  { printf '[INFO]  %s — %s\n' "$(date -Iseconds)" "$*" | tee -a "$ESG_T3_LOG_DIR/runbook.log"; }
log_warn()  { printf '[WARN]  %s — %s\n' "$(date -Iseconds)" "$*" | tee -a "$ESG_T3_LOG_DIR/runbook.log" >&2; }
log_error() { printf '[ERROR] %s — %s\n' "$(date -Iseconds)" "$*" | tee -a "$ESG_T3_LOG_DIR/runbook.log" >&2; }

# dry-run 모드 처리
ESG_T3_DRY_RUN="${ESG_T3_DRY_RUN:-false}"
for arg in "$@"; do
    case "$arg" in
        --dry-run) ESG_T3_DRY_RUN=true ;;
        --apply)   ESG_T3_DRY_RUN=false ;;
    esac
done

dry_run_or_exec() {
    if [[ "$ESG_T3_DRY_RUN" == "true" ]]; then
        log_info "[DRY-RUN] $*"
    else
        log_info "[EXEC] $*"
        eval "$@"
    fi
}

# Runbook 실행을 AuditLog에 기록 (실 구현은 Phase 2 audit 모듈 완성 후)
audit_runbook_execution() {
    local script="$1"
    local tenant_id="${2:-system}"
    log_info "AUDIT: runbook script=$script tenant=$tenant_id"
    # TODO Phase 2: POST /api/v1/admin/audit/runbook → audit_logs 기록
}

# SUPER_ADMIN 토큰 확인
require_super_admin_token() {
    if [[ -z "${ESG_T3_SUPER_ADMIN_TOKEN:-}" ]]; then
        log_error "ESG_T3_SUPER_ADMIN_TOKEN 환경 변수가 필요합니다."
        exit 1
    fi
}
