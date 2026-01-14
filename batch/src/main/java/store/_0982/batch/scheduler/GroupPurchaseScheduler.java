package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("local")
@Slf4j
@RequiredArgsConstructor
@Component
public class GroupPurchaseScheduler {
    private final JobLauncher jobLauncher;
    private final Job groupPurchaseJob;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledGroupPurchase() throws Exception{
        jobLauncher.run(groupPurchaseJob, new JobParameters());
    }
}
