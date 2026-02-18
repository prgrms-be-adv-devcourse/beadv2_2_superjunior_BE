import http from 'k6/http';
import { check, sleep } from 'k6';

export { handleSummary } from '../common/summary.js';

const BASE_URL = __ENV.TARGET_URL || 'https://0982.store';
const DEFAULT_VUS = Number.parseInt(__ENV.VUS || '10', 10) || 10;
const DEFAULT_DURATION = __ENV.DURATION || '1m';
const PAGE_SIZE = Number.parseInt(__ENV.PAGE_SIZE || '10', 10) || 10;
const MAX_PAGE_FALLBACK = Math.max(0, Number.parseInt(__ENV.MAX_PAGE || '20', 10) || 20);

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

let maxPageSeen = MAX_PAGE_FALLBACK;

function parseJsonSafe(response) {
    try {
        return response.json();
    } catch (_error) {
        return null;
    }
}

function getRandomPage() {
    return Math.floor(Math.random() * (maxPageSeen + 1));
}

function updateMaxPage(payload) {
    if (!payload || !payload.data) {
        return;
    }

    const totalPages = payload.data.totalPages;
    if (Number.isInteger(totalPages) && totalPages > 0) {
        maxPageSeen = totalPages - 1;
    }
}

export default function () {
    const page = getRandomPage();
    const response = http.get(`${BASE_URL}/api/purchases?page=${page}&size=${PAGE_SIZE}`, {
        tags: {
            endpoint: '/api/purchases',
            scenario: 'random_group_purchase_page',
        },
    });

    const payload = parseJsonSafe(response);
    check(response, {
        'group purchase list status is 200': (r) => r.status === 200,
        'group purchase list response dto': () => payload !== null && payload.data !== undefined,
    });

    updateMaxPage(payload);
    sleep(1);
}
