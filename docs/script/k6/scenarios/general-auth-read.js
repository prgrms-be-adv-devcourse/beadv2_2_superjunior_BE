// noinspection JSUnresolvedReference, JSUnusedGlobalSymbols, NpmUsedModulesInstalled

import http from 'k6/http';
import { check, group, sleep } from 'k6';

export { handleSummary } from '../common/summary.js';

const BASE_URL = __ENV.TARGET_URL || 'https://0982.store';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'aiden915y@gmail.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'qwer1234!';
const ENABLE_PROACTIVE_REFRESH = String(__ENV.ENABLE_PROACTIVE_REFRESH || 'false').toLowerCase() === 'true';

const DEFAULT_VUS = Number.parseInt(__ENV.VUS || '50', 10) || 50;
const DEFAULT_DURATION = __ENV.DURATION || '5m';
const REFRESH_BUFFER_SECONDS = 10;

export const options = {
    stages: [
        { duration: '1m', target: DEFAULT_VUS },
        { duration: DEFAULT_DURATION, target: DEFAULT_VUS },
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'checks': ['rate>0.95'],
        'http_req_duration': ['p(95)<800'],
        'http_req_duration{type:auth_login}': ['p(95)<1000'],
        'http_req_duration{type:auth_refresh}': ['p(95)<1000'],
    },
};

let vuState;

function initVuState() {
    if (vuState) {
        return vuState;
    }

    vuState = {
        authenticated: false,
        accessTokenExpireAtMs: 0,
        jar: http.cookieJar(),
    };
    return vuState;
}

function randomThinkTime() {
    return Math.random() + 0.5;
}

function parseJsonSafe(response) {
    try {
        return response.json();
    } catch (_e) {
        return null;
    }
}

function isResponseDto(payload) {
    return payload !== null
        && typeof payload === 'object'
        && Object.prototype.hasOwnProperty.call(payload, 'status')
        && Object.prototype.hasOwnProperty.call(payload, 'message')
        && Object.prototype.hasOwnProperty.call(payload, 'data');
}

function toSetCookieHeaders(headers) {
    const setCookieValue = headers['Set-Cookie'] || headers['set-cookie'];
    if (!setCookieValue) {
        return [];
    }
    return Array.isArray(setCookieValue) ? setCookieValue : [setCookieValue];
}

function extractCookieMaxAge(headers, cookieName) {
    const setCookieHeaders = toSetCookieHeaders(headers);
    const cookiePrefix = `${cookieName}=`;

    for (const rawHeader of setCookieHeaders) {
        const serialized = String(rawHeader);
        const cookieSegments = serialized.split(/,\s*(?=[^;,\s]+=)/);

        for (const segment of cookieSegments) {
            const value = segment.trim();
            if (!value.startsWith(cookiePrefix)) {
                continue;
            }
            const maxAgeMatch = value.match(/;\s*Max-Age=(\d+)/i);
            if (maxAgeMatch) {
                const parsed = Number.parseInt(maxAgeMatch[1], 10);
                if (Number.isFinite(parsed)) {
                    return parsed;
                }
            }
        }
    }

    return null;
}

function hasCookie(jar, cookieName) {
    const cookies = jar.cookiesForURL(BASE_URL);
    const values = cookies[cookieName];
    return Array.isArray(values) && values.length > 0 && String(values[0]).length > 0;
}

function updateAccessTokenExpiry(state, response) {
    const maxAge = extractCookieMaxAge(response.headers, 'accessToken');
    if (maxAge !== null) {
        state.accessTokenExpireAtMs = Date.now() + (maxAge * 1000);
    }
}

function createRequestParams(state, extraParams) {
    const params = Object.assign({ jar: state.jar }, extraParams || {});
    params.headers = Object.assign({}, (extraParams && extraParams.headers) || {});
    params.tags = Object.assign({}, (extraParams && extraParams.tags) || {});
    return params;
}

function sendRequest(state, method, path, body, extraParams) {
    const params = createRequestParams(state, extraParams);
    const payload = body === null || body === undefined ? null : JSON.stringify(body);
    if (payload !== null && !params.headers['Content-Type']) {
        params.headers['Content-Type'] = 'application/json';
    }
    return http.request(method, `${BASE_URL}${path}`, payload, params);
}

function checkResponseDto(response, name, expectedStatus, requireData) {
    const payload = parseJsonSafe(response);
    const statusCode = expectedStatus || 200;
    const expectData = requireData !== false;

    check(response, {
        [`${name}: status ${statusCode}`]: (r) => r.status === statusCode,
        [`${name}: json body`]: () => payload !== null,
        [`${name}: response dto`]: () => isResponseDto(payload),
    });

    if (expectData) {
        check(payload, {
            [`${name}: data present`]: (p) => p !== null && p.data !== undefined,
        });
    }

    return payload;
}

function login(state) {
    const response = sendRequest(
        state,
        'POST',
        '/auth/login',
        {
            email: LOGIN_EMAIL,
            password: LOGIN_PASSWORD,
        },
        {
            tags: {
                type: 'auth_login',
                endpoint: '/auth/login',
            },
        },
    );

    checkResponseDto(response, 'auth login', 200, false);
    updateAccessTokenExpiry(state, response);

    const cookieOk = hasCookie(state.jar, 'accessToken');
    check({ cookieOk }, {
        'auth login: accessToken cookie issued': (x) => x.cookieOk,
    });

    state.authenticated = response.status === 200 && cookieOk;
    return response;
}

function refresh(state) {
    const response = sendRequest(
        state,
        'GET',
        '/auth/refresh',
        null,
        {
            tags: {
                type: 'auth_refresh',
                endpoint: '/auth/refresh',
            },
        },
    );

    checkResponseDto(response, 'auth refresh', 200, false);
    updateAccessTokenExpiry(state, response);

    const cookieOk = hasCookie(state.jar, 'accessToken');
    check({ cookieOk }, {
        'auth refresh: accessToken cookie available': (x) => x.cookieOk,
    });

    state.authenticated = response.status === 200 && cookieOk;
    return response;
}

function shouldProactivelyRefresh(state) {
    if (!ENABLE_PROACTIVE_REFRESH) {
        return false;
    }
    if (!state.accessTokenExpireAtMs) {
        return false;
    }
    return (Date.now() + (REFRESH_BUFFER_SECONDS * 1000)) >= state.accessTokenExpireAtMs;
}

function ensureAuthenticated(state) {
    if (!state.authenticated || !hasCookie(state.jar, 'accessToken')) {
        const loginResponse = login(state);
        return loginResponse.status === 200;
    }

    if (shouldProactivelyRefresh(state)) {
        const refreshResponse = refresh(state);
        if (refreshResponse.status !== 200) {
            state.authenticated = false;
            const loginResponse = login(state);
            return loginResponse.status === 200;
        }
    }

    return true;
}

function requestWithRecovery(state, method, path, body, extraParams) {
    let response = sendRequest(state, method, path, body, extraParams);
    if (response.status !== 401 && response.status !== 403) {
        return response;
    }

    const refreshResponse = refresh(state);
    if (refreshResponse.status === 200) {
        response = sendRequest(state, method, path, body, extraParams);
        if (response.status !== 401 && response.status !== 403) {
            return response;
        }
    }

    state.authenticated = false;
    const loginResponse = login(state);
    if (loginResponse.status === 200) {
        return sendRequest(state, method, path, body, extraParams);
    }

    return response;
}

function getFirstGroupPurchaseId(payload) {
    const content = payload && payload.data && payload.data.content;
    if (!Array.isArray(content) || content.length === 0) {
        return null;
    }

    const first = content[0];
    if (!first || typeof first !== 'object') {
        return null;
    }

    if (first.groupPurchaseId) {
        return String(first.groupPurchaseId);
    }

    return null;
}

function assertAuthReady(state) {
    const ok = ensureAuthenticated(state);
    check({ ok }, {
        'auth ready before protected calls': (x) => x.ok,
    });
    return ok;
}

export default function () {
    const state = initVuState();

    group('Public Browse', () => {
        const purchasesResponse = sendRequest(
            state,
            'GET',
            '/api/purchases?page=0&size=20',
            null,
            {
                tags: {
                    type: 'public_read',
                    endpoint: '/api/purchases',
                },
            },
        );
        const purchasesPayload = checkResponseDto(purchasesResponse, 'public purchases list', 200, true);

        const groupPurchaseId = getFirstGroupPurchaseId(purchasesPayload);
        if (groupPurchaseId) {
            const detailResponse = sendRequest(
                state,
                'GET',
                `/api/purchases/${groupPurchaseId}`,
                null,
                {
                    tags: {
                        type: 'public_read',
                        endpoint: '/api/purchases/{id}',
                    },
                },
            );
            checkResponseDto(detailResponse, 'public purchases detail', 200, true);
        }

        const searchResponse = sendRequest(
            state,
            'GET',
            '/api/searches/purchase/search?keyword=&page=0&size=20',
            null,
            {
                tags: {
                    type: 'public_read',
                    endpoint: '/api/searches/purchase/search',
                },
            },
        );
        checkResponseDto(searchResponse, 'public purchase search', 200, true);
    });

    sleep(randomThinkTime());

    group('Authenticated Read', () => {
        if (!assertAuthReady(state)) {
            return;
        }

        const profileResponse = requestWithRecovery(
            state,
            'GET',
            '/api/members/profile',
            null,
            {
                tags: {
                    type: 'auth_read',
                    endpoint: '/api/members/profile',
                },
            },
        );
        checkResponseDto(profileResponse, 'member profile', 200, true);

        const roleResponse = requestWithRecovery(
            state,
            'GET',
            '/api/members/role',
            null,
            {
                tags: {
                    type: 'auth_read',
                    endpoint: '/api/members/role',
                },
            },
        );
        checkResponseDto(roleResponse, 'member role', 200, true);

        const unreadResponse = requestWithRecovery(
            state,
            'GET',
            '/api/notifications/unread?page=0&size=20',
            null,
            {
                tags: {
                    type: 'auth_read',
                    endpoint: '/api/notifications/unread',
                },
            },
        );
        checkResponseDto(unreadResponse, 'notification unread', 200, true);

        const cartsResponse = requestWithRecovery(
            state,
            'GET',
            '/api/carts?page=0&size=20',
            null,
            {
                tags: {
                    type: 'auth_read',
                    endpoint: '/api/carts',
                },
            },
        );
        checkResponseDto(cartsResponse, 'carts list', 200, true);

        const ordersResponse = requestWithRecovery(
            state,
            'GET',
            '/api/orders/consumer?page=0&size=20',
            null,
            {
                tags: {
                    type: 'auth_read',
                    endpoint: '/api/orders/consumer',
                },
            },
        );
        checkResponseDto(ordersResponse, 'consumer orders', 200, true);
    });

    sleep(randomThinkTime());
}
