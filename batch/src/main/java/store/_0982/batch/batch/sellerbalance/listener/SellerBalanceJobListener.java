package store._0982.batch.batch.sellerbalance.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

import java.time.Duration;

/**
 * 출금 정산 Job 실행 전후 처리
 */
@Slf4j
@Component
public class SellerBalanceJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(
                BatchLogMessageFormat.jobStart(jobExecution.getJobInstance().getJobName()),
                BatchLogMetadataFormat.jobStart(
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters().toString()
                )
        );
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long duration = -1L;
        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            duration = Duration.between(
                    jobExecution.getStartTime(),
                    jobExecution.getEndTime())
                    .toMillis();
        }

        String jobName = jobExecution.getJobInstance().getJobName();
        Long executionId = jobExecution.getId();

        if (jobExecution.getStatus().isUnsuccessful()) {
            // Job 실패
            String failedStep = jobExecution.getStepExecutions().stream()
                    .filter(step -> step.getStatus().isUnsuccessful())
                    .map(StepExecution::getStepName)
                    .findFirst()
                    .orElse("UNKNOWN");

            String errorMessage = jobExecution.getAllFailureExceptions().stream()
                    .findFirst()
                    .map(Throwable::getMessage)
                    .orElse("Unknown error");

            log.error(
                    BatchLogMessageFormat.jobFailed(jobName),
                    BatchLogMetadataFormat.jobFailed(
                            jobName,
                            executionId,
                            duration,
                            failedStep,
                            errorMessage
                    )
            );
        } else {
            // Job 성공
            log.info(
                    BatchLogMessageFormat.jobSuccess(jobName),
                    BatchLogMetadataFormat.jobSuccess(
                            jobName,
                            executionId,
                            duration,
                            jobExecution.getStatus().toString()
                    )
            );
        }
    }
}
