package store._0982.common.domain.order;

import java.util.EnumSet;

public enum CancelReason {
    // 시스템
    GROUP_PURCHASE_FAILED,

    // 사용자 귀책
    CHANGE_OF_MIND,         // 단순 변심

    // 판매자 귀책
    PRODUCT_DEFECT,         // 상품 하자
    DELIVERY_DELAY,         // 배송 지연
    OUT_OF_STOCK;            // 재고 부족

    private static final EnumSet<CancelReason> BUYER_FAULT =
            EnumSet.of(CHANGE_OF_MIND);

    private static final EnumSet<CancelReason> SELLER_FAULT =
            EnumSet.of(PRODUCT_DEFECT, DELIVERY_DELAY, OUT_OF_STOCK);

    public boolean isBuyerFault() {
        return BUYER_FAULT.contains(this);
    }

    public boolean isSellerFault() {
        return SELLER_FAULT.contains(this);
    }
}
