package store._0982.member.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "적절하지 않은 요청 값이 존재합니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 8자리 이상이며 영어, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "적절한 이메일 형식이 아닙니다."),
    INVALID_NAME(HttpStatus.BAD_REQUEST, "이름은 영문, 한글, 숫자로 구성되고, 2자 이상 50자 이하이어야 합니다."),
    REQUEST_HEADER_IS_NULL(HttpStatus.BAD_REQUEST, "필요한 헤더가 전달되지 않았습니다."),
    // Seller
    INVALID_SELLER_ACCOUNT_NUMBER(HttpStatus.BAD_REQUEST, "계좌번호는 숫자만 입력 가능합니다."),
    INVALID_SELLER_BANK_CODE(HttpStatus.BAD_REQUEST, "은행 코드는 숫자만 입력 가능합니다."),
    INVALID_SELLER_ACCOUNT_HOLDER(HttpStatus.BAD_REQUEST, "예금주는 50자 미만의 한글과 영어만 입력 가능합니다."),
    INVALID_SELLER_BUSINESS_REGISTRATION_NUMBER(HttpStatus.BAD_REQUEST, "사업자 등록번호는 XXX-XX-XXXXX 형식으로 입력해야 합니다."),

    // 409 Conflict
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일입니다."),
    DUPLICATED_NAME(HttpStatus.BAD_REQUEST, "이미 사용중인 이름입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),

    // 401 Unauthorized
    NO_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
    NO_EMAIL_INFO(HttpStatus.UNAUTHORIZED, "이메일 정보가 없습니다."),
    NO_ROLE_INFO(HttpStatus.UNAUTHORIZED, "유저 역할 정보가 없습니다."),

    //403 비밀번호 틀림
    WRONG_PASSWORD(HttpStatus.FORBIDDEN, "틀린 비밀번호입니다."),

    //404
    NOT_EXIST_MEMBER(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
