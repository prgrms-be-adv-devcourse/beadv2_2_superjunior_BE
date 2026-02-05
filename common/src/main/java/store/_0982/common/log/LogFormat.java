package store._0982.common.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogFormat {

    private static final String REQUEST_WITHOUT_MEMBER = "[REQUEST] [%s %s]";
    private static final String REQUEST_WITH_MEMBER = REQUEST_WITHOUT_MEMBER + " [member:%s]";
    private static final String RESPONSE_WITHOUT_MEMBER = "[RESPONSE] [%s] [%s %s] [duration:%dms]";
    private static final String RESPONSE_WITH_MEMBER = RESPONSE_WITHOUT_MEMBER + " [member:%s]";
    private static final String SERVICE_COMPLETE = "[SERVICE] [%s] [duration:%dms] completed";
    private static final String SERVICE_FAIL = "[SERVICE] [%s] [duration:%dms] failed";
    private static final String ERROR = "[ERROR] [%s] %s";

    public static String request(String httpMethod, String uri) {
        return String.format(REQUEST_WITHOUT_MEMBER, httpMethod, uri);
    }

    public static String request(String httpMethod, String uri, String memberId) {
        if (memberId == null) {
            return request(httpMethod, uri);
        }
        return String.format(REQUEST_WITH_MEMBER, httpMethod, uri, memberId);
    }

    public static String response(HttpStatus status, String httpMethod, String uri, long responseTimeMs) {
        return String.format(RESPONSE_WITHOUT_MEMBER, status, httpMethod, uri, responseTimeMs);
    }

    public static String response(HttpStatus status, String httpMethod, String uri, long responseTimeMs, String memberId) {
        if (memberId == null) {
            return response(status, httpMethod, uri, responseTimeMs);
        }
        return String.format(RESPONSE_WITH_MEMBER, status, httpMethod, uri, responseTimeMs, memberId);
    }

    public static String serviceComplete(String methodName, long endTimeMs) {
        return String.format(SERVICE_COMPLETE, methodName, endTimeMs);
    }

    public static String serviceFail(String methodName, long endTimeMs) {
        return String.format(SERVICE_FAIL, methodName, endTimeMs);
    }

    public static String error(HttpStatus status, String message) {
        return String.format(ERROR, status, message);
    }
}
