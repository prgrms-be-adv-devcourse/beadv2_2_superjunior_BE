package store._0982.batch.domain.settlement;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;

public record SettlementPeriod(OffsetDateTime start, OffsetDateTime end) {

    public static SettlementPeriod ofLastMonth(ZoneId zone) {
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

        return new SettlementPeriod(periodStart, periodEnd);
    }
}
