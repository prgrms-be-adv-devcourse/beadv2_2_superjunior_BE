package store._0982.member.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),

    //401 토큰 없음
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
    //403 비밀번호 틀림
    WRONG_PASSWORD(HttpStatus.FORBIDDEN, "틀린 비밀번호입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
