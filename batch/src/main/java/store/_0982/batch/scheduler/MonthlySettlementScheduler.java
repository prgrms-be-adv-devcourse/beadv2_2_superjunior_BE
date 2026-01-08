package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("local")
@RequiredArgsConstructor
@Component
public class MonthlySettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void scheduleMonthlySettlement() throws Exception {
        jobLauncher.run(monthlySettlementJob, new JobParameters());
    }
}
