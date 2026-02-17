// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import {check, sleep} from 'k6';

export {handleSummary} from '../common/summary.js'

// CI/CD 테스트용 간단 설정
export const options = {
    scenarios: {
        search: {
            executor: 'ramping-arrival-rate',
            timeUnit: '1s',
            preAllocatedVUs: 100,
            maxVUs: 200,
            stages: [
                {duration: '30s', target: 50},  // Ramp-up
                {duration: '2m', target: 50},   // Steady 1
                {duration: '30s', target: 100}, // Ramp-up 2
                {duration: '5m', target: 100},  // Steady 2
                {duration: '30s', target: 150}, // Ramp-up 3
                {duration: '5m', target: 150},  // Steady 3
                {duration: '30s', target: 0},  // Ramp-down
            ],
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<2000'],  // 95%가 2초 이내
        'http_req_failed': ['rate<0.05'],     // 에러율 5% 미만
        'checks': ['rate>0.90'],              // 체크 통과율 90% 이상
    },
};

const BASE_URL = __ENV.TARGET_URL || 'https://0982.store.app';
const KEYWORDS = ['고양이'];

export default function () {
    const keyword = KEYWORDS[__ITER % KEYWORDS.length];
    const searchRes = http.get(
            `${BASE_URL}/api/searches/purchase/search?keyword=${encodeURIComponent(keyword)}`
    );

    let responseOk = false;
    let json = null;
    try {
        json = searchRes.json();
        responseOk = json && json.status === 200 && json.data !== undefined;
    } catch (e) {
        responseOk = false;
    }

    check(searchRes, {
        'search status is 200': (r) => r.status === 200,
        'search response time < 1s': (r) => r.timings.duration < 1000,
        'search response payload is ok': () => responseOk,
    });

    sleep(1);
}


