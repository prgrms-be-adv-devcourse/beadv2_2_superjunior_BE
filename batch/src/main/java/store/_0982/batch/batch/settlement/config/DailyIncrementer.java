package store._0982.batch.batch.settlement.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;
import store._0982.batch.batch.sellerpayout.policy.SellerPayoutPolicy;

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

        String date = LocalDate.now(SellerPayoutPolicy.KOREA_ZONE).toString();

        return builder
                .addString("date", date)
                .addLong("executionTime", ZonedDateTime.now(SellerPayoutPolicy.KOREA_ZONE).toInstant().toEpochMilli(), false)
                .toJobParameters();
    }
}
