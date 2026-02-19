import http from 'k6/http';
import { check, sleep } from 'k6';

export { handleSummary } from '../common/summary.js';

const BASE_URL = __ENV.TARGET_URL || 'https://0982.store';
const DEFAULT_VUS = Number.parseInt(__ENV.VUS || '10', 10) || 10;
const DEFAULT_DURATION = __ENV.DURATION || '1m';

export const options = {
    stages: [
        { duration: '30s', target: DEFAULT_VUS },
        { duration: DEFAULT_DURATION, target: DEFAULT_VUS },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        'http_req_duration': ['p(95)<2000'],
        'http_req_failed': ['rate<0.05'],
        'checks': ['rate>0.90'],
    },
};

function parseJsonSafe(response) {
    try {
        return response.json();
    } catch (_error) {
        return null;
    }
}

export default function () {
    const response = http.get(`${BASE_URL}/api/recommendations`, {
        tags: {
            endpoint: '/api/recommendations',
            scenario: 'get_recommendations',
        },
    });

    const payload = parseJsonSafe(response);
    check(response, {
        'recommendations status is 200': (r) => r.status === 200,
        'recommendations response is json': () => payload !== null,
    });

    sleep(1);
}
