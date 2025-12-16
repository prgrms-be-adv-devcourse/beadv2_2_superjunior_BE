package store._0982.gateway.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {
    // 400 Bad Request
    EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID(HttpStatus.UNAUTHORIZED, "비정상 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "내부용 api에 접근 권한이 없습니다." );

    private final HttpStatus httpStatus;
    private final String message;
}
