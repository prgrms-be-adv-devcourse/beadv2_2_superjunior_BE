package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Profile("dev")
@RequiredArgsConstructor
@Component
public class GroupPurchaseScheduler {
    private final JobLauncher jobLauncher;
    private final Job groupPurchaseJob;

    @Scheduled(cron = "0 */2 * * * *")
    public void scheduledGroupPurchase() throws Exception{
        jobLauncher.run(groupPurchaseJob,
                new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .addString("now", OffsetDateTime.now().toString())
                        .toJobParameters());
    }
}
