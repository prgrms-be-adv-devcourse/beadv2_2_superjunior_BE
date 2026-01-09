package store._0982.batch.batch.settlement.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;

/**
 * 타임스탬프 기반 JobParametersIncrementer
 */
public class TimestampIncrementer implements JobParametersIncrementer {

    @NonNull
    @Override
    public JobParameters getNext(JobParameters parameters) {
        JobParametersBuilder builder =
                (parameters == null)
                        ? new JobParametersBuilder()
                        : new JobParametersBuilder(parameters);

        return builder
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
