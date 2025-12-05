package store._0982.point.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST),
    PAYMENT_KEY_ISNULL(HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN),

    // 404 Not Found
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND),

    // 409 Conflict
    COMPLETED_PAYMENT(HttpStatus.CONFLICT),
    DIFFERENT_AMOUNT(HttpStatus.CONFLICT),
    ORDER_ID_MISMATCH(HttpStatus.CONFLICT),

    // 500 Internal Server Error
    PAYMENT_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
    CHECK_POINT_HISTORY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_COMPLETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus httpStatus;
}
