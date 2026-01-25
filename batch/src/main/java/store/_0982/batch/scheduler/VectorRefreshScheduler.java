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

@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Component
public class VectorRefreshScheduler {

    private final JobLauncher jobLauncher;
    private final Job vectorRefreshJob;

    //    @Scheduled(cron = "* * * * * *", zone = "Asia/Seoul")
    @Scheduled(initialDelay = 7000, fixedDelay = 6000000)
    public void scheduleVectorRefresh() throws Exception {
        log.info("벡터 배치 시작");
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(vectorRefreshJob, jobParameters);
    }
}
