package store._0982.common.log;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

public final class LogFormat {
    private static final String REQUEST_WITHOUT_MEMBER = "[REQUEST] [%s %s]";
    private static final String REQUEST_WITH_MEMBER = REQUEST_WITHOUT_MEMBER + " [member:%s]";
    private static final String RESPONSE_WITHOUT_MEMBER = "[RESPONSE] [%s] [%s %s] [duration:%dms]";
    private static final String RESPONSE_WITH_MEMBER = RESPONSE_WITHOUT_MEMBER + " [member:%s]";
    private static final String SERVICE_START = "[SERVICE] [%s] [params:%s]";
    private static final String SERVICE_COMPLETE = "[SERVICE] [%s] [duration:%dms] completed";
    private static final String SERVICE_FAIL = "[SERVICE] [%s] [duration:%dms] failed";
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

    public static String responseOf(HttpStatus status, String httpMethod, String uri, long responseTimeMs) {
        return String.format(RESPONSE_WITHOUT_MEMBER, status, httpMethod, uri, responseTimeMs);
    }

    public static String responseOf(HttpStatus status, String httpMethod, String uri, long responseTimeMs, String memberId) {
        if (memberId == null) {
            return responseOf(status, httpMethod, uri, responseTimeMs);
        }
        return String.format(RESPONSE_WITH_MEMBER, status, httpMethod, uri, responseTimeMs, memberId);
    }

    public static String serviceStartOf(String methodName, Object[] args) {
        return String.format(SERVICE_START, methodName, Arrays.toString(args));
    }

    public static String serviceCompleteOf(String methodName, long endTimeMs) {
        return String.format(SERVICE_COMPLETE, methodName, endTimeMs);
    }

    public static String serviceFailOf(String methodName, long endTimeMs) {
        return String.format(SERVICE_FAIL, methodName, endTimeMs);
    }

    public static String errorOf(HttpStatus status, String message) {
        return String.format(ERROR, status, message);
    }

    private LogFormat() {
    }
}
