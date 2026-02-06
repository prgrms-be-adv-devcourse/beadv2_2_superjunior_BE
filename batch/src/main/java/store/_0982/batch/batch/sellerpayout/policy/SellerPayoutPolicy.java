package store._0982.batch.batch.sellerpayout.policy;

import java.time.ZoneId;

public class SellerPayoutPolicy {

    public static final int CHUNK_UNIT = 10;

    public static final int MAX_RETRY = 5;

    public static final long MINIMUM_TRANSFER_AMOUNT = 30000L;

    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private SellerPayoutPolicy() {}

}
