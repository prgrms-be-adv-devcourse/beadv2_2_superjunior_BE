// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import {check, sleep} from 'k6';

export {generateSummary as handleSummary} from '../common/summary.js';

// CI/CD 테스트용 간단한 설정
export const options = {
    stages: [
        {duration: '30s', target: parseInt(__ENV.VUS) || 10},  // Ramp-up
        {duration: __ENV.DURATION || '1m', target: parseInt(__ENV.VUS) || 10},  // Steady
        {duration: '30s', target: 0},  // Ramp-down
    ],
    thresholds: {
        'http_req_duration': ['p(95)<2000'],  // 95%가 2초 이내
        'http_req_failed': ['rate<0.05'],     // 에러율 5% 미만
        'checks': ['rate>0.90'],              // 체크 통과율 90% 이상
    },
};

const BASE_URL = 'https://test.k6.io';

export default function () {
    // 1. 메인 페이지 조회
    const mainRes = http.get(BASE_URL);
    check(mainRes, {
        'main page status is 200': (r) => r.status === 200,
        'main page response time < 1s': (r) => r.timings.duration < 1000,
    });

    sleep(1);

    // 2. API 테스트 (contacts 엔드포인트)
    const contactsRes = http.get(`${BASE_URL}/contacts.php`);
    check(contactsRes, {
        'contacts status is 200': (r) => r.status === 200,
        'contacts response time < 1s': (r) => r.timings.duration < 1000,
    });

    sleep(1);

    // 3. News 페이지 조회
    const newsRes = http.get(`${BASE_URL}/news.php`);
    check(newsRes, {
        'news status is 200': (r) => r.status === 200,
        'news has content': (r) => r.body && r.body.length > 0,
    });

    sleep(1);
}
