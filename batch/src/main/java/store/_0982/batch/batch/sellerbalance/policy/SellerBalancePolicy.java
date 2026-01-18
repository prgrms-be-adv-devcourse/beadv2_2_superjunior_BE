package store._0982.batch.batch.sellerbalance.policy;

import java.time.LocalDate;
import java.time.ZoneId;

public class SellerBalancePolicy {

    public static final int CHUNK_UNIT = 100;

    public static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    public static LocalDate getTwoWeeksAgo() {
        return LocalDate.now(KOREA_ZONE).minusWeeks(2);
    }

    private SellerBalancePolicy() {}

}
