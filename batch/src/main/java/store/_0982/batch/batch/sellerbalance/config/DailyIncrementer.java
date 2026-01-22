package store._0982.batch.batch.sellerbalance.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;
import store._0982.batch.batch.settlement.policy.SettlementPolicy;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public class DailyIncrementer implements JobParametersIncrementer {

    @NonNull
    @Override
    public JobParameters getNext(JobParameters parameters) {
        JobParametersBuilder builder =
                (parameters == null)
                        ? new JobParametersBuilder()
                        : new JobParametersBuilder(parameters);

        String date = LocalDate.now(SettlementPolicy.KOREA_ZONE).toString();

        return builder
                .addString("date", date)
                .addLong("executionTime", ZonedDateTime.now(SettlementPolicy.KOREA_ZONE).toInstant().toEpochMilli(), false)
                .toJobParameters();
    }
}
