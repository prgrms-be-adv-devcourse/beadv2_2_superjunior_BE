package store._0982.common.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELED = "order.canceled";

    /**
     * 주문 생성, 취소 외에 상태 변경을 의미합니다.
     */
    public static final String ORDER_CHANGED = "order.changed";

    public static final String POINT_CHANGED = "point.changed";

    public static final String PAYMENT_CHANGED = "payment.changed";

    public static final String PRODUCT_UPSERTED = "product.upserted";

    @Deprecated(forRemoval = true)
    public static final String PRODUCT_DELETED = "product.deleted";

    public static final String PRODUCT_EMBEDDING_COMPLETED = "product.embedding.completed";

    /**
     * @deprecated {@link KafkaTopics#GROUP_PURCHASE_CHANGED}에서 같이 관리합니다.
     */
    @Deprecated(forRemoval = true)
    public static final String GROUP_PURCHASE_STATUS_CHANGED = "group-purchase.changed";

    /**
     * @deprecated {@link KafkaTopics#GROUP_PURCHASE_CHANGED}에서 같이 이용해 주세요.
     */
    @Deprecated(forRemoval = true)
    public static final String GROUP_PURCHASE_CREATED = "group-purchase.created";
    public static final String GROUP_PURCHASE_CHANGED = "group-purchase.update";

    public static final String MEMBER_DELETED = "member.deleted";
    public static final String MEMBER_LOGGED_IN = "member.logged-in";

    @Deprecated(forRemoval = true)
    public static final String DAILY_SETTLEMENT_COMPLETED = "settlement.daily.completed";

    @Deprecated(forRemoval = true)
    public static final String DAILY_SETTLEMENT_FAILED = "settlement.daily.failed";

    @Deprecated(forRemoval = true)
    public static final String MONTHLY_SETTLEMENT_COMPLETED = "settlement.monthly.completed";

    @Deprecated(forRemoval = true)
    public static final String MONTHLY_SETTLEMENT_FAILED = "settlement.monthly.failed";

    public static final String SELLER_BALANCE_CHANGED = "seller-balance.changed";

    public static final String SETTLEMENT_DONE = "settlement.done";
}
