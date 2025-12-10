package store._0982.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {
    // 400 Bad request
    CANNOT_READ(HttpStatus.BAD_REQUEST, "읽을 수 없는 알림입니다."),

    // 403 Forbidden
    NO_PERMISSION_TO_READ(HttpStatus.FORBIDDEN, "해당 유저의 알림이 아닙니다."),

    // 404 Not found
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
