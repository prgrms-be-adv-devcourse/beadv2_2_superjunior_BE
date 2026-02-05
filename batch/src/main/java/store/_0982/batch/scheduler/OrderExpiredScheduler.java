package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class OrderExpiredScheduler {

    private final JobLauncher jobLauncher;
    private final Job orderExpiredJob;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void scheduleOrderExpired() throws Exception{
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(orderExpiredJob, jobParameters);
    }
}
