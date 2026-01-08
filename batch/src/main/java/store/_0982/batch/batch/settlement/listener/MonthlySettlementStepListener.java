package store._0982.batch.batch.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import store._0982.common.log.BatchLogFormat;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 월간 정산 Step 실행 전후 처리
 */
@Slf4j
@Component
public class MonthlySettlementStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(BatchLogFormat.STEP_START,
                stepExecution.getStepName(),
                stepExecution.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        LocalDateTime start = stepExecution.getStartTime();
        LocalDateTime end = stepExecution.getEndTime();

        long duration = -1L;
        if (start != null && end != null) {
            duration = Duration.between(start, end).toMillis();
        }

        String stepName = stepExecution.getStepName();
        long readCount = stepExecution.getReadCount();
        long writeCount = stepExecution.getWriteCount();

        if (stepExecution.getStatus().isUnsuccessful()) {
            // Step 실패
            log.error(BatchLogFormat.STEP_FAILED,
                    stepName,
                    readCount,
                    writeCount,
                    stepExecution.getExitStatus().getExitDescription());

        } else {
            // Step 성공
            log.info(BatchLogFormat.STEP_SUCCESS,
                    stepName,
                    readCount,
                    writeCount,
                    duration);
        }

        return stepExecution.getExitStatus();
    }
}
