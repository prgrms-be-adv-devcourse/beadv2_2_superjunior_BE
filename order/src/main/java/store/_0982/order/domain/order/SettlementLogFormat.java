package store._0982.order.domain.order;

public class SettlementLogFormat {

    public static final String START = "[SCHEDULER] [{}] started";
    public static final String COMPLETE = "[SCHEDULER] [{}] completed";
    public static final String FAIL = "[SCHEDULER] [{}] failed";

    public static final String DAILY_SETTLEMENT_START = "[DAILY_SETTLEMENT] [Seller:{}] started - {}";
    public static final String DAILY_SETTLEMENT_COMPLETE = "[DAILY_SETTLEMENT] [Seller:{}] completed";
    public static final String DAILY_SETTLEMENT_FAIL = "[DAILY_SETTLEMENT] [Seller:{}] failed - {}";

    public static final String MONTHLY_SETTLEMENT_COMPLETE = "[MONTHLY_SETTLEMENT] [Seller:{}] completed";
    public static final String MONTHLY_SETTLEMENT_FAIL = "[MONTHLY_SETTLEMENT] [Seller:{}] failed - {}";

    private SettlementLogFormat() {}
}
