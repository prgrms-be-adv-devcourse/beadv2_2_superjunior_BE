package store._0982.batch.batch.settlement.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;

import java.time.YearMonth;
import java.time.ZonedDateTime;

/**
 * 월간 정산용 JobParametersIncrementer
 * - 같은 달에는 동일한 파라미터를 생성하여 중복 실행 방지
 * - 년월 기반으로 Job 인스턴스 구분 (예: "2026-01")
 */
public class TimestampIncrementer implements JobParametersIncrementer {

    @NonNull
    @Override
    public JobParameters getNext(JobParameters parameters) {
        JobParametersBuilder builder =
                (parameters == null)
                        ? new JobParametersBuilder()
                        : new JobParametersBuilder(parameters);

        String yearMonth = YearMonth.now(SettlementPolicy.KOREA_ZONE).toString();

        return builder
                .addString("yearMonth", yearMonth)
                .addLong("executionTime", ZonedDateTime.now(SettlementPolicy.KOREA_ZONE).toInstant().toEpochMilli(), false)
                .toJobParameters();
    }
}
