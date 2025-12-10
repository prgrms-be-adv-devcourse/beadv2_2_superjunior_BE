package store._0982.common.kafka;

public final class KafkaTopics {
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_STATUS_CHANGED = "order.changed";

    public static final String POINT_RECHARGED = "point.recharged";
    public static final String POINT_CHANGED = "point.changed";

    public static final String PRODUCT_ADDED = "product.added";

    public static final String LOG_PUBLISHED = "log.published";

    public static final String GROUP_PURCHASE_ADDED = "group-purchase.added";
    public static final String GROUP_PURCHASE_STATUS_CHANGED = "group-purchase.changed";

    public static final String DAILY_SETTLEMENT_COMPLETED = "settlement.daily.completed";
    public static final String DAILY_SETTLEMENT_FAILED = "settlement.daily.failed";
    public static final String MONTHLY_SETTLEMENT_COMPLETED = "settlement.monthly.completed";
    public static final String MONTHLY_SETTLEMENT_FAILED = "settlement.monthly.failed";

    private KafkaTopics() {
    }
}
