import http from 'k6/http';
import { check, sleep } from 'k6';

export { handleSummary } from '../common/summary.js';

const BASE_URL = __ENV.TARGET_URL || 'https://0982.store';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'aiden915y@gmail.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'qwer1234!';
const DEFAULT_VUS = Number.parseInt(__ENV.VUS || '10', 10) || 10;
const DEFAULT_DURATION = __ENV.DURATION || '1m';

let vuState;

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

function initVuState() {
    if (vuState) {
        return vuState;
    }

    vuState = {
        initialLoginDone: false,
        jar: http.cookieJar(),
    };
    return vuState;
}

function loginOncePerVu(state) {
    if (state.initialLoginDone) {
        return;
    }

    const response = http.post(
        `${BASE_URL}/auth/login`,
        JSON.stringify({
            email: LOGIN_EMAIL,
            password: LOGIN_PASSWORD,
        }),
        {
            jar: state.jar,
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                endpoint: '/auth/login',
                scenario: 'get_recommendations',
                type: 'auth_login',
            },
        },
    );

    check(response, {
        'auth login status is 200': (r) => r.status === 200,
    });

    state.initialLoginDone = true;
}

function refreshAuth(state) {
    const response = http.get(`${BASE_URL}/auth/refresh`, {
        jar: state.jar,
        tags: {
            endpoint: '/auth/refresh',
            scenario: 'get_recommendations',
            type: 'auth_refresh',
        },
    });

    check(response, {
        'auth refresh status is 200': (r) => r.status === 200,
    });
}

function requestRecommendations(state, retried) {
    return http.get(`${BASE_URL}/api/recommendations`, {
        jar: state.jar,
        tags: {
            endpoint: '/api/recommendations',
            scenario: 'get_recommendations',
            retried: retried ? 'true' : 'false',
        },
    });
}

export default function () {
    const state = initVuState();
    loginOncePerVu(state);

    let response = requestRecommendations(state, false);
    if (response.status === 401 || response.status === 403) {
        refreshAuth(state);
        response = requestRecommendations(state, true);
    }

    const payload = parseJsonSafe(response);
    check(response, {
        'recommendations status is 200': (r) => r.status === 200,
        'recommendations response is json': () => payload !== null,
    });

    sleep(1);
}
