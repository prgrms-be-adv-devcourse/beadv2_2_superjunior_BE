package store._0982.batch.batch.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import store._0982.common.log.BatchLogFormat;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 월간 정산 Job 실행 전후 처리
 */
@Slf4j
@Component
public class MonthlySettlementJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(BatchLogFormat.JOB_START,
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime start = jobExecution.getStartTime();
        LocalDateTime end = jobExecution.getEndTime();

        long duration = -1L;
        if (start != null && end != null) {
            duration = Duration.between(start, end).toMillis();
        }

        String jobName = jobExecution.getJobInstance().getJobName();
        Long executionId = jobExecution.getId();

        if (jobExecution.getStatus().isUnsuccessful()) {
            // Job 실패
            log.error(BatchLogFormat.JOB_FAILED,
                    jobName,
                    executionId,
                    duration,
                    jobExecution.getExitStatus().getExitCode(),
                    jobExecution.getExitStatus().getExitDescription());

        } else {
            // Job 성공
            log.info(BatchLogFormat.JOB_SUCCESS,
                    jobName,
                    executionId,
                    duration,
                    jobExecution.getStatus());
        }
    }
}
