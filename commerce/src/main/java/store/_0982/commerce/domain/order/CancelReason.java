package store._0982.commerce.domain.order;

public enum CancelReason {
    // 시스템
    GROUP_PURCHASE_FAILED,

    // 사용자 귀책
    CHANGE_OF_MIND,         // 단순 변심

    // 판매자 귀책
    PRODUCT_DEFECT,         // 상품 하자
    DELIVERY_DELAY,         // 배송 지연
    OUT_OF_STOCK            // 재고 부족
}
