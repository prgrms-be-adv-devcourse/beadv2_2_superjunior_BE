package store._0982.point.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND),

    // 409 Conflict
    CONFLICT(HttpStatus.CONFLICT),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE);

    private final HttpStatus httpStatus;
}
