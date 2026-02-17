// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import {check, sleep} from 'k6';

export {handleSummary} from '../common/summary.js'

export const options = {
    stages: [
        {duration: '30s', target: parseInt(__ENV.VUS) || 100},  // Ramp-up
        {duration: __ENV.DURATION || '5m', target: parseInt(__ENV.VUS) || 100},  // Steady
        {duration: '30s', target: 0},  // Ramp-down
    ],
    thresholds: {
        'http_req_duration': ['p(95)<8000'],  // 95%가 8초 이내
        'http_req_failed': ['rate<0.05'],     // 에러율 5% 미만
        'checks': ['rate>0.90'],              // 체크 통과율 90% 이상
    },
};

const BASE_URL = __ENV.TARGET_URL_DB || __ENV.TARGET_URL || 'https://0982.store.app';
const KEYWORDS = ['고양이'];

export default function () {
    const keyword = KEYWORDS[__ITER % KEYWORDS.length];
    const searchRes = http.get(
            `${BASE_URL}/api/purchases/search/db?keyword=${encodeURIComponent(keyword)}`
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
        'search response time < 5s': (r) => r.timings.duration < 5000,
        'search response payload is ok': () => responseOk,
    });

    sleep(1);
}
