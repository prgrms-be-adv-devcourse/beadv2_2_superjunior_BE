package store._0982.point.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 금액입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "적절하지 않은 요청 값이 존재합니다."),
    REQUEST_HEADER_IS_NULL(HttpStatus.BAD_REQUEST, "필요한 헤더가 전달되지 않았습니다."),
    PAYMENT_KEY_IS_NULL(HttpStatus.BAD_REQUEST, "PaymentKey 값이 없습니다."),
    ORDER_ID_IS_NULL(HttpStatus.BAD_REQUEST, "OrderId 값이 없습니다."),

    LACK_OF_POINT(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    REFUND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "환불 조건에 맞지 않아 환불이 불가합니다."),

    // 401 Unauthorized
    NO_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
    NO_EMAIL_INFO(HttpStatus.UNAUTHORIZED, "이메일 정보가 없습니다."),
    NO_ROLE_INFO(HttpStatus.UNAUTHORIZED, "유저 역할 정보가 없습니다."),

    // 403 Forbidden
    PAYMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "해당 결제의 유저가 아닙니다."),

    // 404 Not Found
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 요청을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

    // 409 Conflict
    ALREADY_COMPLETED_PAYMENT(HttpStatus.CONFLICT, "이미 완료된 결제입니다."),
    CANNOT_HANDLE_FAILURE(HttpStatus.CONFLICT, "결제가 이미 완료 또는 환불되었습니다."),
    DIFFERENT_AMOUNT(HttpStatus.CONFLICT, "결제 금액이 불일치합니다."),
    ORDER_ID_MISMATCH(HttpStatus.CONFLICT, "주문 번호가 일치하지 않습니다."),
    NOT_COMPLETED_PAYMENT(HttpStatus.CONFLICT, "환불할 수 없는 상태입니다."),

    // 500 Internal Server Error
    PAYMENT_COMPLETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 502 Bad Gateway
    PAYMENT_API_ERROR(HttpStatus.BAD_GATEWAY, "결제 API 호출에 실패했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),

    // 504
    PAYMENT_API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "결제 API 호출이 주어진 시간에 완료되지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
