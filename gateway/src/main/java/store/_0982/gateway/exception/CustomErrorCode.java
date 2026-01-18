package store._0982.gateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {
    // 401 Bad Request
    INVALID(HttpStatus.UNAUTHORIZED, "비정상 토큰입니다."),
    // 403
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
