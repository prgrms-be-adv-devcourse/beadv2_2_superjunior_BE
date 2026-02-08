// noinspection JSUnresolvedReference

import {htmlReport} from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

/**
 * k6 테스트 결과를 HTML 리포트와 텍스트 요약으로 생성
 *
 * @param {Object} data - k6 summary data
 * @returns {Object} 파일 경로를 키로, 내용을 값으로 하는 객체
 */
export function generateSummary(data) {
    const timestamp = __ENV.K6_TIMESTAMP || new Date().toISOString().replace(/[:.]/g, '-');

    return {
        [`/opt/k6/results/report-${timestamp}.html`]: htmlReport(data),
    };
}
