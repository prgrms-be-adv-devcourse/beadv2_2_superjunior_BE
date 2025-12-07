package store._0982.point.common.log;

import org.springframework.http.HttpStatus;

public final class LogFormat {
    private static final String REQUEST_WITHOUT_MEMBER = "[REQUEST] [%s] %s";
    private static final String REQUEST_WITH_MEMBER = REQUEST_WITHOUT_MEMBER + " by %s";
    private static final String RESPONSE_WITHOUT_MEMBER = "[RESPONSE] [%s] %s - %dms";
    private static final String RESPONSE_WITH_MEMBER = RESPONSE_WITHOUT_MEMBER + " to %s";
    private static final String ERROR = "[ERROR] [%s] %s";

    public static String requestOf(String httpMethod, String uri) {
        return String.format(REQUEST_WITHOUT_MEMBER, httpMethod, uri);
    }

    public static String requestOf(String httpMethod, String uri, String memberId) {
        if (memberId == null) {
            return requestOf(httpMethod, uri);
        }
        return String.format(REQUEST_WITH_MEMBER, httpMethod, uri, memberId);
    }

    public static String responseOf(String httpMethod, String uri, long responseTimeMs) {
        return String.format(RESPONSE_WITHOUT_MEMBER, httpMethod, uri, responseTimeMs);
    }

    public static String responseOf(String httpMethod, String uri, long responseTimeMs, String memberId) {
        if (memberId == null) {
            return responseOf(httpMethod, uri, responseTimeMs);
        }
        return String.format(RESPONSE_WITH_MEMBER, httpMethod, uri,  responseTimeMs, memberId);
    }

    public static String errorOf(HttpStatus status, String message) {
        return String.format(ERROR, status, message);
    }

    private LogFormat() {
    }
}
