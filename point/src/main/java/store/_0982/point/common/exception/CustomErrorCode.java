package store._0982.point.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 충전 금액입니다."),
    PAYMENT_KEY_ISNULL(HttpStatus.BAD_REQUEST, "paymentKey는 필수입니다."),
    LACK_OF_POINT(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    NOT_COMPLTED_PAYMENT(HttpStatus.BAD_REQUEST, "완료 상태의 결제가 아닙니다."),
    REFUND_AFTER_ORDER(HttpStatus.BAD_REQUEST,"결제 이후 포인트 사용 내역이 있습니다."),

    // 401 Unauthorized
    NO_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),

    // 403 Forbidden
    PAYMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "해당 결제의 유저가 아닙니다."),
    // 404 Not Found
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 요청을 찾을 수 없습니다."),
    HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 충전 내역이 없습니다."),
    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "잘못된 페이지 번호입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),

    // 409 Conflict
    COMPLETED_PAYMENT(HttpStatus.CONFLICT, "이미 완료된 결제입니다."),
    DIFFERENT_AMOUNT(HttpStatus.CONFLICT, "결제 금액이 불일치합니다."),
    ORDER_ID_MISMATCH(HttpStatus.CONFLICT, "주문 번호가 일치하지 않습니다."),

    // 500 Internal Server Error
    PAYMENT_COMPLETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인 중 오류가 발생했습니다.");

    // 503 Service Unavailable

    private final HttpStatus httpStatus;
    private final String message;
}
