package store._0982.batch.batch.ai.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;

import javax.annotation.Nullable;

public class VectorRefreshJobIncrementer implements JobParametersIncrementer {

    @NonNull
    @Override
    public JobParameters getNext(@Nullable JobParameters parameters) {
        JobParametersBuilder builder =
                (parameters == null)
                        ? new JobParametersBuilder()
                        : new JobParametersBuilder(parameters);

        return builder
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
