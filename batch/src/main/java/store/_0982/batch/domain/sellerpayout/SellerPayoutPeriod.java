package store._0982.batch.domain.sellerpayout;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public record SellerPayoutPeriod(OffsetDateTime start, OffsetDateTime end) {

    public static SellerPayoutPeriod ofLastMonth(ZoneId zone) {
        YearMonth lastMonth = YearMonth.now(zone).minusMonths(1);

        OffsetDateTime periodStart = lastMonth
                .atDay(1)
                .atStartOfDay(zone)
                .toOffsetDateTime();

        OffsetDateTime periodEnd = lastMonth
                .atEndOfMonth()
                .atTime(23, 59, 59)
                .atZone(zone)
                .toOffsetDateTime();

        return new SellerPayoutPeriod(periodStart, periodEnd);
    }
}
