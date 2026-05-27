#!/usr/bin/env bash
# 모든 Prometheus 알림 규칙의 runbook URL이 실제 파일을 가리키는지 검증.
# CI에서 호출. 누락 시 exit 1.

set -euo pipefail

readonly REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
readonly ALERTS_DIR="$REPO_ROOT/infra/observability/alerts"

fail=0

# 알림 yml에서 runbook: "docs/runbook/XX.md" 추출하여 검증
while IFS= read -r path; do
    if [[ ! -f "$REPO_ROOT/$path" ]]; then
        echo "[FAIL] Runbook 파일 누락: $path"
        fail=1
    else
        echo "[OK]   $path"
    fi
done < <(grep -hroE 'runbook:\s*"[^"]+"' "$ALERTS_DIR" | sed -E 's/runbook:\s*"([^"]+)"/\1/')

if [[ $fail -ne 0 ]]; then
    echo
    echo "[ERROR] 일부 알림의 Runbook URL이 누락된 파일을 가리킵니다. 알림 yml 또는 Runbook 파일을 추가하세요."
    exit 1
fi

echo
echo "[SUCCESS] 모든 알림의 Runbook 링크가 유효합니다."
