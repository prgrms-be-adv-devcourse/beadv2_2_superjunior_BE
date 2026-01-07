package store._0982.commerce.domain.sellerbalance;

public class SellerBalanceLogFormat {

    public static final String START = "[SCHEDULER] [{}] started";
    public static final String COMPLETE = "[SCHEDULER] [{}] completed";
    public static final String FAIL = "[SCHEDULER] [{}] failed";

    public static final String DAILY_SETTLEMENT_START = "[DAILY_SETTLEMENT] [Seller:{}] started - 공동구매 수: {}";
    public static final String DAILY_SETTLEMENT_COMPLETE = "[DAILY_SETTLEMENT] [Seller:{}] completed - 성공: {}, 실패 - {}";
    public static final String DAILY_SETTLEMENT_FAIL = "[DAILY_SETTLEMENT] [Seller:{}] failed - 공동구매: {}, 오류: {}";

    private SellerBalanceLogFormat() {}
}
