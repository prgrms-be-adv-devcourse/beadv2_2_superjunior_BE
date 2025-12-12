package store._0982.order.domain;

public class SettlementLogFormat {

    public static final String START = "[SCHEDULER] [%s] started";
    public static final String COMPLETE = "[SCHEDULER] [%s] completed";
    public static final String FAIL = "[SCHEDULER] [%s] failed";

    public static final String DAILY_SETTLEMENT_START = "[DAILY_SETTLEMENT] [Seller:%s] started - %d";
    public static final String DAILY_SETTLEMENT_COMPLETE = "[DAILY_SETTLEMENT] [Seller:%s] completed";
    public static final String DAILY_SETTLEMENT_FAIL = "[DAILY_SETTLEMENT] [Seller:%s] failed - %s";

    private SettlementLogFormat() {}
}
