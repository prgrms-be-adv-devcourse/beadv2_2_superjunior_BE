package store._0982.batch.batch.grouppurchase.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.lang.NonNull;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class GroupPurchaseJobIncrementer implements JobParametersIncrementer {

    @NonNull
    @Override
    public JobParameters getNext(@Nullable JobParameters parameters) {
        JobParametersBuilder builder =
                (parameters == null)
                        ? new JobParametersBuilder()
                        : new JobParametersBuilder(parameters);

        return builder
                .addLong("timestamp", System.currentTimeMillis())
                .addString("now", OffsetDateTime.now().toString())
                .toJobParameters();
    }
}
