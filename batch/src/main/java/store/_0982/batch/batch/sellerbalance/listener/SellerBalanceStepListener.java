package store._0982.batch.batch.sellerbalance.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

import java.time.Duration;
import java.util.Set;

/**
 * 일간 정산 Step 실행 전후 처리
 */
@Slf4j
@Component
public class SellerBalanceStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        String jobName = jobExecution.getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();
        Long jobExecutionId = stepExecution.getJobExecutionId();

        log.info(
                BatchLogMessageFormat.stepStart(jobName, stepName),
                BatchLogMetadataFormat.stepStart(
                        jobName,
                        stepName,
                        jobExecutionId
                )
        );
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        String jobName = jobExecution.getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();

        long duration = -1L;
        if (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null) {
            duration = Duration.between(
                    stepExecution.getStartTime(),
                    stepExecution.getEndTime()
            ).toMillis();
        }

        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();
        long skipCount = stepExecution.getSkipCount();

        if (stepExecution.getStatus().isUnsuccessful()) {
            String errorMessage = stepExecution.getFailureExceptions()
                    .stream()
                    .findFirst()
                    .map(Throwable::getMessage)
                    .orElse("UNKNOWN");

            log.error(
                    BatchLogMessageFormat.stepFailed(jobName, stepName),
                    BatchLogMetadataFormat.stepFailed(
                            jobName,
                            stepName,
                            readCount,
                            writeCount,
                            skipCount,
                            duration,
                            errorMessage
                    ));
        }
        else {
            log.info(
                    BatchLogMessageFormat.stepSuccess(jobName, stepName),
                    BatchLogMetadataFormat.stepSuccess(
                            jobName,
                            stepName,
                            readCount,
                            writeCount,
                            skipCount,
                            duration
                    )
            );
        }

        return stepExecution.getExitStatus();
    }
}
