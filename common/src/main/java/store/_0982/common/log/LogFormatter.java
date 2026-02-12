package store._0982.common.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogFormatter {

    public static LogFormatInfo request(String httpMethod, String uri, String memberId) {
        return LogFormatInfo.of(
                LogMessageFormat.REQUEST,
                LogMetadataFormat.request(httpMethod, uri, memberId)
        );
    }

    public static LogFormatInfo response(String httpMethod, String uri, HttpStatus status, long executionTime, String memberId) {
        return LogFormatInfo.of(
                LogMessageFormat.RESPONSE,
                LogMetadataFormat.response(httpMethod, uri, status, executionTime, memberId)
        );
    }

    public static LogFormatInfo serviceComplete(String methodName, long executionTime) {
        return LogFormatInfo.of(
                LogMessageFormat.SERVICE_COMPLETE,
                LogMetadataFormat.serviceComplete(methodName, executionTime)
        );
    }

    public static LogFormatInfo serviceFail(String methodName, long executionTime) {
        return LogFormatInfo.of(
                LogMessageFormat.SERVICE_FAIL,
                LogMetadataFormat.serviceFail(methodName, executionTime)
        );
    }

    public static LogFormatInfo error(HttpStatus status, String errorMessage) {
        return LogFormatInfo.of(
                LogMessageFormat.ERROR,
                LogMetadataFormat.error(status, errorMessage)
        );
    }

    public static LogFormatInfo error(HttpStatus status, Throwable throwable) {
        return LogFormatInfo.of(
                LogMessageFormat.ERROR,
                LogMetadataFormat.error(status, throwable)
        );
    }
}
