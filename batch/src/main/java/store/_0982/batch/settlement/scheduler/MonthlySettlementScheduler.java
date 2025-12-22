package store._0982.batch.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.batch.infrastructure.settlement.SettlementLogFormat;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlySettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    /**
     * 매월 1일 02:00에 월별 정산 배치 실행
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void scheduleMonthlySettlement() {
        String schedulerName = "MonthlySettlement";
        log.info(SettlementLogFormat.START, schedulerName);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("executionTime", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(monthlySettlementJob, jobParameters);
            log.info(SettlementLogFormat.COMPLETE, schedulerName);
        } catch (Exception e) {
            log.error(SettlementLogFormat.FAIL, schedulerName);
        }
    }
}
