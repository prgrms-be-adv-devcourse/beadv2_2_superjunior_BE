package store._0982.batch.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_QUANTITY_RANGE(HttpStatus.BAD_REQUEST, "잘못된 수량입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "잘못된 날짜 범위입니다."),
    INVALID_OPEN_PURCHASE_UPDATE(HttpStatus.BAD_REQUEST, "공동 구매가 OPEN 상태입니다."),
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "상품명이 유효하지 않습니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격이 유효하지 않습니다."),
    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "카테고리가 유효하지 않습니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고가 유효하지 않습니다."),

    // 404 Not Found
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    GROUPPURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "공동구매를 찾을 수 없습니다."),

    // 403 Forbidden
    NON_SELLER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다. 판매자 권한이 필요합니다."),
    FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 상품이 아닙니다."),
    FORBIDDEN_NOT_GROUP_PURCHASE_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 공동구매만 삭제할 수 있습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),
    MEMBER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Member 서비스를 사용할 수 없습니다."),


    //400 Bad Request
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "잘못된 수량입니다."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "잘못된 주소입니다."),
    POSTAL_CODE_IS_NULL(HttpStatus.BAD_REQUEST, "우편 주소가 없습니다."),
    INVALID_RECEIVER_NAME(HttpStatus.BAD_REQUEST, "잘못된 수신자 이름입니다."),
    SELLER_ID_IS_NULL(HttpStatus.BAD_REQUEST, "SellerId 값이 없습니다."),
    GROUP_PURCHASE_ID_IS_NULL(HttpStatus.BAD_REQUEST, "GroupPurchaseId 값이 없습니다."),
    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 정산 금액입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "정산 잔액이 부족합니다."),
    INVALID_ACCOUNT_INFO(HttpStatus.BAD_REQUEST, "유효하지 않은 계좌 정보입니다."),
    GROUP_PURCHASE_IS_NOT_OPEN(HttpStatus.BAD_REQUEST, "공동 구매가 시작하지 않았습니다."),
    GROUP_PURCHASE_IS_END(HttpStatus.BAD_REQUEST, "종료된 공동 구매입니다."),
    GROUP_PURCHASE_IS_REACHED(HttpStatus.BAD_REQUEST, "공동구매 참여 인원이 최대입니다."),
    ORDER_NOT_CANCELLABLE(HttpStatus.BAD_REQUEST, "취소할 수 없습니다. 공동 구매가 시작되었습니다."),
    LACK_OF_POINT(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    CART_IS_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),

    // 403 Forbidden
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN,"본인의 주문내역만 조회할 수 있습니다."),
    NOT_CART_OWNER(HttpStatus.FORBIDDEN, "카트에 대한 권한이 없습니다."),

    // 404 Not Found
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자를 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에서 해당 공동구매를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
