// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import {check, sleep, group} from 'k6';

export {generateSummary as handleSummary} from '../common/summary.js';

// 환경변수로 동적 설정 가능
export const options = {
    stages: [
        {duration: '1m', target: parseInt(__ENV.VUS) || 100},   // Ramp-up
        {duration: __ENV.DURATION || '5m', target: parseInt(__ENV.VUS) || 100}, // Steady state
        {duration: '1m', target: 0},                            // Ramp-down
    ],
    thresholds: {
        'http_req_duration': ['p(95)<500'],
        'http_req_failed': ['rate<0.01'],
        'checks': ['rate>0.95'],
    },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8000';

export default function () {
    // 상품 검색 시나리오
    group('Product Search', () => {
        const searchRes = http.get(`${BASE_URL}/api/searches/products?keyword=공구`);
        check(searchRes, {
            'search status is 200': (r) => r.status === 200,
            'search response time < 500ms': (r) => r.timings.duration < 500,
        });
    });

    sleep(1);

    // 상품 상세 조회 시나리오
    group('Product Detail', () => {
        const detailRes = http.get(`${BASE_URL}/api/products/1`);
        check(detailRes, {
            'detail status is 200': (r) => r.status === 200,
            'detail response time < 300ms': (r) => r.timings.duration < 300,
        });
    });

    sleep(1);
}
