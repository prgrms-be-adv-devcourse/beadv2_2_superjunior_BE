package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@RequiredArgsConstructor
@Component
public class SellerPayoutScheduler {

    private final JobLauncher jobLauncher;
    private final Job sellerPayoutJob;

//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void scheduleSellerPayout() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(sellerPayoutJob, jobParameters);
    }
}
