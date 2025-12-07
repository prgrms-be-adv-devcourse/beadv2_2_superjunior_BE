package store._0982.gateway.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {
    // 400 Bad Request
    EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID(HttpStatus.UNAUTHORIZED, "비정상 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
