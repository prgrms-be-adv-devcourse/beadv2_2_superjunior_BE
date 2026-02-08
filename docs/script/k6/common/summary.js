// noinspection JSUnresolvedReference, JSUrlImportUsage

import { htmlReport } from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js'
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js'

/**
 * k6 테스트 결과를 HTML 리포트와 텍스트 요약으로 생성
 *
 * @param {Object} data - k6 summary data
 * @returns {Object} 파일 경로를 키로, 내용을 값으로 하는 객체
 */
export function generateSummary(data) {
    return {
        'result.html': htmlReport(data),
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
    }
}
