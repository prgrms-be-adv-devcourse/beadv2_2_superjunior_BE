package store._0982.point.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NegligibleWebhookErrorType {

    INVALID_EVENT_TYPE("유효하지 않은 이벤트 타입입니다."),
    PAYMENT_NOT_FOUND("존재하지 않는 주문에 대한 웹훅 이벤트입니다."),
    PAYMENT_STATUS_MISMATCH("웹훅 이벤트와 저장된 결제 상태가 호환되지 않습니다.");

    private final String message;
}
