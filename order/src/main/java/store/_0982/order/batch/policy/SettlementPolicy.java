package store._0982.order.batch.policy;

import java.time.ZoneId;

public class SettlementPolicy {

    public static final long MINIMUM_TRANSFER_AMOUNT = 30000L;

    public static final int SERVICE_FEE_RATE = 20;

    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static long calculateServiceFee(long settlementBalance) {
        return settlementBalance / SERVICE_FEE_RATE;
    }

    public static long calculateTransferAmount(long settlementBalance) {
        long serviceFee = calculateServiceFee(settlementBalance);
        return settlementBalance - serviceFee;
    }

    private SettlementPolicy() {}

}
