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
public class VectorRefreshScheduler {

    private final JobLauncher jobLauncher;
    private final Job vectorRefreshJob;

//    @Scheduled(cron = "* * * * * *", zone = "Asia/Seoul") 1일 1회 오전 3시 13분
    public void scheduleVectorRefresh() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(vectorRefreshJob, jobParameters);
    }
}
