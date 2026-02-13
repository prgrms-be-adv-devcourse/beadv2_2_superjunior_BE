package store._0982.common.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.logstash.logback.argument.StructuredArgument;
import org.springframework.http.HttpStatus;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMetadataFormat {

    private static final String HTTP_METHOD = "httpMethod";
    private static final String ENDPOINT = "endpoint";
    private static final String MEMBER_ID = "memberId";
    private static final String STATUS = "status";
    private static final String EXECUTION_TIME = "executionTime";
    private static final String SERVICE_METHOD_NAME = "serviceMethod";
    private static final String IS_COMPLETED = "isCompleted";
    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String STACK_TRACE = "errorStackTrace"; //stackTrace는 이미 고정된 키워드 사용자 필드로 사용 금지

    public static StructuredArgument[] request(String httpMethod, String endpoint) {
        return new StructuredArgument[]{
                keyValue(HTTP_METHOD, httpMethod),
                keyValue(ENDPOINT, endpoint)
        };
    }

    public static StructuredArgument[] request(String httpMethod, String endpoint, String memberId) {
        if (memberId == null) {
            return request(httpMethod, endpoint);
        }
        return new StructuredArgument[]{
                keyValue(HTTP_METHOD, httpMethod),
                keyValue(ENDPOINT, endpoint),
                keyValue(MEMBER_ID, memberId)
        };
    }

    public static StructuredArgument[] response(String httpMethod, String endpoint, HttpStatus status, long executionTime) {
        return new StructuredArgument[]{
                keyValue(HTTP_METHOD, httpMethod),
                keyValue(ENDPOINT, endpoint),
                keyValue(STATUS, status),
                keyValue(EXECUTION_TIME, executionTime),
        };
    }
    public static StructuredArgument[] response(String httpMethod, String endpoint, HttpStatus status, long executionTime, String memberId) {
        if (memberId == null) {
            return request(httpMethod, endpoint);
        }
        return new StructuredArgument[]{
                keyValue(HTTP_METHOD, httpMethod),
                keyValue(ENDPOINT, endpoint),
                keyValue(STATUS, status),
                keyValue(EXECUTION_TIME, executionTime),
                keyValue(MEMBER_ID, memberId)
        };
    }

    public static StructuredArgument[] serviceComplete(String methodName, long executionTime) {
        return new StructuredArgument[]{
                keyValue(SERVICE_METHOD_NAME, methodName),
                keyValue(EXECUTION_TIME, executionTime),
                keyValue(IS_COMPLETED, true)
        };
    }

    public static StructuredArgument[] serviceFail(String methodName, long executionTime) {
        return new StructuredArgument[]{
                keyValue(SERVICE_METHOD_NAME, methodName),
                keyValue(EXECUTION_TIME, executionTime),
                keyValue(IS_COMPLETED, false)
        };
    }

    public static StructuredArgument[] error(HttpStatus status, String errorMessage) {
        return new StructuredArgument[]{
                keyValue(ERROR_MESSAGE, errorMessage),
                keyValue(STATUS, status)
        };
    }

    public static StructuredArgument[] error(HttpStatus status, Throwable throwable) {

        return new StructuredArgument[]{
                keyValue(ERROR_MESSAGE, throwable.getMessage()),
                keyValue(STATUS, status),
                keyValue(STACK_TRACE, Stream.of(throwable.getStackTrace()).limit(10)
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining(", "))
                )
        };
    }
}
