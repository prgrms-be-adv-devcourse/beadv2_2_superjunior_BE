package store._0982.batch.batch.settlement.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;

import java.time.LocalDateTime;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementJobListener 단위 테스트")
class SettlementJobListenerTest {

    private SettlementJobListener listener;

    @BeforeEach
    void setUp() {
        listener = new SettlementJobListener();
    }

    @Test
    @DisplayName("beforeJob 호출 시 예외 없이 실행된다")
    void beforeJob_shouldRunWithoutException() {
        // given
        JobExecution jobExecution = createJobExecution("settlementJob", BatchStatus.STARTED);

        // when & then
        listener.beforeJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - Job 성공 시 예외 없이 실행된다")
    void afterJob_whenCompleted_shouldRunWithoutException() {
        // given
        JobExecution jobExecution = createJobExecution("settlementJob", BatchStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.now().minusMinutes(1));
        jobExecution.setEndTime(LocalDateTime.now());

        // when & then
        listener.afterJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - Job 실패 시 실패한 Step 정보와 함께 예외 없이 실행된다")
    void afterJob_whenFailed_shouldRunWithoutException() {
        // given
        JobExecution jobExecution = createJobExecution("settlementJob", BatchStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now().minusMinutes(1));
        jobExecution.setEndTime(LocalDateTime.now());
        jobExecution.addFailureException(new RuntimeException("정산 처리 중 오류"));

        StepExecution stepExecution = new StepExecution("settlementStep", jobExecution);
        stepExecution.setStatus(BatchStatus.FAILED);
        jobExecution.addStepExecutions(Collections.singletonList(stepExecution));

        // when & then
        listener.afterJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - 실패한 Step이 없어도 UNKNOWN으로 예외 없이 실행된다")
    void afterJob_whenFailedWithNoFailedStep_shouldFallbackToUnknown() {
        // given
        JobExecution jobExecution = createJobExecution("settlementJob", BatchStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now().minusMinutes(1));
        jobExecution.setEndTime(LocalDateTime.now());

        // when & then
        listener.afterJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - 시작/종료 시간이 없어도 예외 없이 실행된다")
    void afterJob_withoutTimeInfo_shouldRunWithoutException() {
        // given
        JobExecution jobExecution = createJobExecution("settlementJob", BatchStatus.COMPLETED);

        // when & then
        listener.afterJob(jobExecution);
    }

    private JobExecution createJobExecution(String jobName, BatchStatus status) {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobInstance jobInstance = new JobInstance(1L, jobName);
        JobExecution jobExecution = new JobExecution(jobInstance, 1L, params);
        jobExecution.setStatus(status);
        return jobExecution;
    }
}
