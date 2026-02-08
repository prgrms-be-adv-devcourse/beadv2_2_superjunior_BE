#!/bin/bash
set -e
exec > >(tee /var/log/k6-test.log) 2>&1

# 환경변수 설정 (__???__는 깃허브 워크플로우를 통해 주입됨)
export TIMESTAMP="__TIMESTAMP__"
export S3_BUCKET="__S3_BUCKET__"
export AWS_REGION="__AWS_REGION__"
export VUS="__VIRTUAL_USERS__"
export DURATION="__DURATION__"
export TARGET_URL="__TARGET_URL__"
export K6_TIMESTAMP="__TIMESTAMP__"
export INSTANCE_TYPE="__INSTANCE_TYPE__"
export TEST_SCENARIO="__TEST_SCENARIO__"

echo "K6 Load Test - __TIMESTAMP__"
echo "Instance Type: __INSTANCE_TYPE__"
echo "VUs: $VUS, Duration: $DURATION"

# S3에서 전체 k6 스크립트 디렉토리 다운로드
mkdir -p /tmp/k6-scripts
aws s3 sync "s3://${S3_BUCKET}/scripts/${TIMESTAMP}/" /tmp/k6-scripts/ --region ${AWS_REGION}

# k6 실행 (디렉토리 구조 유지)
cd /tmp/k6-scripts
k6 run "scenarios/${TEST_SCENARIO}.js" \
  --out json="/opt/k6/results/result-${TIMESTAMP}.json" \
  --summary-export="/opt/k6/results/summary-${TIMESTAMP}.json" \
  || EXIT_CODE=$?

# 결과를 S3에 업로드
if [ -f "/opt/k6/results/result-${TIMESTAMP}.json" ]; then
  aws s3 cp "/opt/k6/results/result-${TIMESTAMP}.json" "s3://${S3_BUCKET}/k6-results/" --region ${AWS_REGION}
fi

if [ -f "/opt/k6/results/summary-${TIMESTAMP}.json" ]; then
  aws s3 cp "/opt/k6/results/summary-${TIMESTAMP}.json" "s3://${S3_BUCKET}/k6-results/" --region ${AWS_REGION}
fi

if [ -f "/opt/k6/results/report-${TIMESTAMP}.html" ]; then
  aws s3 cp "/opt/k6/results/report-${TIMESTAMP}.html" "s3://${S3_BUCKET}/k6-reports/" --region ${AWS_REGION}
fi

# 메타데이터 조회
TOKEN=$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
INSTANCE_ID=$(curl -H "X-aws-ec2-metadata-token: $TOKEN" -s http://169.254.169.254/latest/meta-data/instance-id)

# 메타데이터 생성
cat > /tmp/metadata.json << METADATA
{
  "timestamp": "$TIMESTAMP",
  "scenario": "__TEST_SCENARIO__",
  "vus": $VUS,
  "duration": "$DURATION",
  "target_url": "$TARGET_URL",
  "instance_type": "$INSTANCE_TYPE",
  "instance_id": "$INSTANCE_ID",
  "exit_code": ${EXIT_CODE:-0},
  "github_run_id": "__GITHUB_RUN_ID__",
  "completed_at": "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
}
METADATA

# S3에 업로드
aws s3 cp /tmp/metadata.json s3://${S3_BUCKET}/k6-results/metadata-${TIMESTAMP}.json --region ${AWS_REGION}
aws s3 cp /var/log/k6-test.log s3://${S3_BUCKET}/k6-results/test-${TIMESTAMP}.log --region ${AWS_REGION}
echo "SUCCESS" | aws s3 cp - s3://${S3_BUCKET}/k6-results/complete-${TIMESTAMP} --region ${AWS_REGION}

# 인스턴스 종료
aws ec2 terminate-instances --instance-ids "$INSTANCE_ID" --region ${AWS_REGION}
