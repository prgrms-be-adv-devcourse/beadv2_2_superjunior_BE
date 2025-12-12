package store._0982.order.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {

    //400 Bad Request
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "잘못된 수량입니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "잘못된 금액입니다."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "잘못된 주소입니다."),
    POSTAL_CODE_IS_NULL(HttpStatus.BAD_REQUEST, "우편 주소가 없습니다."),
    INVALID_RECEIVER_NAME(HttpStatus.BAD_REQUEST, "잘못된 수신자 이름입니다."),
    SELLER_ID_IS_NULL(HttpStatus.BAD_REQUEST, "SellerId 값이 없습니다."),
    GROUP_PURCHASE_ID_IS_NULL(HttpStatus.BAD_REQUEST, "GroupPurchaseId 값이 없습니다."),
    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 정산 금액입니다."),
    GROUP_PURCHASE_IS_NOT_OPEN(HttpStatus.BAD_REQUEST, "공동 구매가 시작하지 않았습니다."),
    GROUP_PURCHASE_IS_END(HttpStatus.BAD_REQUEST, "종료된 공동 구매입니다."),
    GROUP_PURCHASE_IS_REACHED(HttpStatus.BAD_REQUEST, "공동구매 참여 인원이 최대입니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "취소할 수 없습니다. 공동 구매가 시작되었습니다."),

    // 404 Not Found
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    GROUPPURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "공동구매를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
