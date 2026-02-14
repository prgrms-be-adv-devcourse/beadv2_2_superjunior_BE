#!/bin/bash
set -e
exec > >(tee /logs/run-batch-test.log) 2>&1

# 환경변수 확인
if [ -z "$JOB_NAMES_INPUT" ] || [ -z "$TIMESTAMP" ]; then
  echo "❌ Required environment variables missing: JOB_NAMES_INPUT, TIMESTAMP"
  exit 1
fi

NODE_NAME="batch-server-${TIMESTAMP}"
NAMESPACE="batch-test-${TIMESTAMP}"

export NODE_SELECTOR="${NODE_NAME}"

# 쉼표로 구분된 job_names를 공백으로 변환
JOBS=$(echo "${JOB_NAMES_INPUT}" | tr ',' ' ')
IFS=' ' read -r -a JOB_ARRAY <<< "$JOBS"
TOTAL_JOBS=${#JOB_ARRAY[@]}

echo "🚀 Deploying batch jobs: $JOBS"
if [ "${JOB_INTERVAL}" -gt 0 ]; then
  echo "⏱️ Interval between jobs: ${JOB_INTERVAL} seconds"
fi

# Job 배포
echo ""
echo "========================================="
if [ "${JOB_INTERVAL}" -eq 0 ]; then
  echo "📦 Deploying batch jobs (parallel execution)"
else
  echo "📦 Deploying batch jobs (staggered execution)"
fi
echo "========================================="

CURRENT_INDEX=0
for job in $JOBS; do
  # 공백 제거
  job=$(echo "$job" | xargs)
  CURRENT_INDEX=$((CURRENT_INDEX + 1))

  JOB_FILE="docs/k8s/job/${job}-job.yml"

  # Job 파일 존재 여부 확인
  if [ ! -f "${JOB_FILE}" ]; then
    echo "❌ Job file not found: ${JOB_FILE}"
    echo "Available jobs: monthly-settlement, seller-balance, retry-settlement, group-purchase, vector-refresh"
    exit 1
  fi

  UNIQUE_JOB_NAME="${job}-job-${TIMESTAMP}"

  # Job 배포
  envsubst < "${JOB_FILE}" | \
    sed "s/name: ${job}-job/name: ${UNIQUE_JOB_NAME}/" | \
    sed "s/namespace: default/namespace: ${NAMESPACE}/" | \
    kubectl apply -n "${NAMESPACE}" -f -

  echo "✅ Deployed ${UNIQUE_JOB_NAME} (${CURRENT_INDEX}/${TOTAL_JOBS})"

  # 마지막 Job이 아니고 간격이 설정된 경우 대기
  if [ ${CURRENT_INDEX} -lt "${TOTAL_JOBS}" ] && [ "${JOB_INTERVAL}" -gt 0 ]; then
    echo "⏳ Waiting ${JOB_INTERVAL} seconds before next job..."
    sleep "${JOB_INTERVAL}"
  fi
done

echo ""
echo "========================================="
echo "⏳ Waiting for all jobs to complete..."
echo "========================================="

# 모든 Job 완료 대기 (1시간 타임아웃)
kubectl wait --for=condition=complete \
  jobs -l purpose=batch-test \
  -n "${NAMESPACE}" \
  --timeout=3600s

echo ""
echo "========================================="
echo "📊 Final Job Status:"
echo "========================================="
kubectl get jobs -n "${NAMESPACE}" -l purpose=batch-test

# 실패한 Job 확인
FAILED_JOBS=$(kubectl get jobs -n "${NAMESPACE}" -l purpose=batch-test -o jsonpath='{.items[?(@.status.failed>0)].metadata.name}')

if [ -n "$FAILED_JOBS" ]; then
  echo ""
  echo "❌ Failed jobs detected: $FAILED_JOBS"
  echo "📋 Logs from failed jobs:"
  for failed_job in $FAILED_JOBS; do
    echo "--- Logs for $failed_job ---"
    kubectl logs -n "${NAMESPACE}" "job/${failed_job}" --tail=100 || true
  done
  exit 1
fi

echo ""
echo "✅ All batch jobs completed successfully!"
