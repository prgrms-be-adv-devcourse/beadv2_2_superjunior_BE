// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import {check, sleep, group} from 'k6';

export {generateSummary as handleSummary} from '../common/summary.js';

// 인덱스 성능 측정을 위한 부하 테스트 설정
export const options = {
    stages: [
        {duration: '1m', target: parseInt(__ENV.VUS) || 100},                   // Ramp-up
        {duration: __ENV.DURATION || '5m', target: parseInt(__ENV.VUS) || 100}, // Steady state
        {duration: '1m', target: 0},                                            // Ramp-down
    ],
    thresholds: {
        'http_req_duration': ['p(95)<200', 'p(99)<500'],
        'http_req_failed': ['rate<0.01'],   // 실패율 1% 미만
        'checks': ['rate>0.95'],            // 검증 통과율 95% 이상

        // 시나리오별 성능 측정
        'http_req_duration{scenario:balance_history}': ['p(95)<200'],
        'http_req_duration{scenario:balance_history_pagination}': ['p(95)<250'],
    },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8000';
const SELLER_TOKEN = __ENV.SELLER_TOKEN || '';

// 판매자 토큰 확인
if (!SELLER_TOKEN) {
    throw new Error('SELLER_TOKEN 환경변수가 필요합니다. 예: k6 run -e SELLER_TOKEN=your-token seller-balance-history-test.js');
}

export default function () {
    const headers = {
        'Cookie': `accessToken=${SELLER_TOKEN}`,
    };

    // 시나리오 1: 기본 잔액 이력 조회 (100건)
    group('Balance History - Default (100 rows)', () => {
        const res = http.get(
            `${BASE_URL}/api/balances/history?page=0&size=100`,
            {
                headers: headers,
                tags: {scenario: 'balance_history'},
            }
        );

        check(res, {
            '[Default] status is 200': (r) => r.status === 200,
            '[Default] response time < 200ms': (r) => r.timings.duration < 200,
            '[Default] response time < 500ms': (r) => r.timings.duration < 500,
            '[Default] has data': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.data && body.data.content;
                } catch (e) {
                    return false;
                }
            },
        });
    });

    sleep(1);

    // 시나리오 2: 페이지네이션 테스트 (다양한 페이지)
    group('Balance History - Pagination', () => {
        const randomPage = Math.floor(Math.random() * 5); // 0-4 페이지 랜덤
        const res = http.get(
            `${BASE_URL}/api/balances/history?page=${randomPage}&size=50`,
            {
                headers: headers,
                tags: {scenario: 'balance_history_pagination'},
            }
        );

        check(res, {
            '[Pagination] status is 200': (r) => r.status === 200,
            '[Pagination] response time < 250ms': (r) => r.timings.duration < 250,
            '[Pagination] has pagination info': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.data && typeof body.data.totalElements === 'number';
                } catch (e) {
                    return false;
                }
            },
        });
    });

    sleep(1);
}
