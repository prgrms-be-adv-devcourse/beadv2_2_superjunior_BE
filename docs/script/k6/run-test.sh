#!/bin/bash
# k6 테스트 실행 헬퍼

SCRIPT_PATH=$1
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
RESULT_FILE="/opt/k6/results/result-${TIMESTAMP}.json"
SUMMARY_FILE="/opt/k6/results/summary-${TIMESTAMP}.json"
HTML_FILE="/opt/k6/results/report-${TIMESTAMP}.html"

if [ -z "$SCRIPT_PATH" ]; then
    echo "Usage: $0 <script-path>"
    exit 1
fi

# S3에서 스크립트 다운로드 (s3:// 프로토콜인 경우)
if [[ $SCRIPT_PATH == s3://* ]]; then
    LOCAL_SCRIPT="/tmp/test-script.js"
    aws s3 cp "$SCRIPT_PATH" "$LOCAL_SCRIPT"
    SCRIPT_PATH="$LOCAL_SCRIPT"
fi

# k6 실행
echo "Running k6 test: $SCRIPT_PATH"
K6_TIMESTAMP="$TIMESTAMP" k6 run "$SCRIPT_PATH" \
    --out json="$RESULT_FILE" \
    --summary-export="$SUMMARY_FILE"

# 결과를 S3에 업로드 (환경변수로 버킷 지정)
if [ -n "$S3_BUCKET" ]; then
    echo "Uploading results to S3..."

    if [ -f "$RESULT_FILE" ]; then
        aws s3 cp "$RESULT_FILE" "s3://$S3_BUCKET/k6-results/"
    fi

    if [ -f "$SUMMARY_FILE" ]; then
        aws s3 cp "$SUMMARY_FILE" "s3://$S3_BUCKET/k6-results/"
    fi

    if [ -f "$HTML_FILE" ]; then
        aws s3 cp "$HTML_FILE" "s3://$S3_BUCKET/k6-reports/"
    fi
fi

echo "Test completed. Results: $RESULT_FILE"
