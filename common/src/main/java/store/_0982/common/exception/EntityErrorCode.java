package store._0982.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EntityErrorCode implements ErrorCode {

    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 정산 금액입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "정산 잔액이 부족합니다."),

    DECREASE_QUANTITY_FAILED(HttpStatus.BAD_REQUEST, "공동구매 수량 감소에 실패했습니다."),

    CANNOT_PAYMENT_COMPLETED_ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "결제 진행 중 상태 주문만 결제 완료 처리할 수 있습니다."),
    CANNOT_PAYMENT_FAILED_ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "결제 진행 중 상태 주문만 결제 실패 처리할 수 있습니다."),
    CANNOT_PURCHASE_CONFIRM_ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "공구 성공 상태의 주문만 구매 확정할 수 있습니다."),
    INVALID_CANCEL_STATUS(HttpStatus.BAD_REQUEST, "취소 요청 상태가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
