package store._0982.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DefaultErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "적절하지 않은 요청 값이 존재합니다."),
    REQUEST_HEADER_IS_NULL(HttpStatus.BAD_REQUEST, "필요한 헤더가 전달되지 않았습니다."),

    // 401 Unauthorized
    NO_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
    NO_EMAIL_INFO(HttpStatus.UNAUTHORIZED, "이메일 정보가 없습니다."),
    NO_ROLE_INFO(HttpStatus.UNAUTHORIZED, "유저 역할 정보가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
