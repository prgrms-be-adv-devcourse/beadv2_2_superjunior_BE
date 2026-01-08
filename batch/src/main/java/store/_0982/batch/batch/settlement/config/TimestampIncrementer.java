package store._0982.batch.batch.settlement.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * 타임스탬프 기반 JobParametersIncrementer
 */
public class TimestampIncrementer implements JobParametersIncrementer {

    @Override
    public JobParameters getNext(JobParameters parameters) {
        return new JobParametersBuilder(parameters)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
