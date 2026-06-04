package store._0982.batch.batch.settlement.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementStepListener 단위 테스트")
class SettlementStepListenerTest {

    private SettlementStepListener listener;

    @BeforeEach
    void setUp() {
        listener = new SettlementStepListener();
    }

    @Test
    @DisplayName("beforeStep 호출 시 예외 없이 실행된다")
    void beforeStep_shouldRunWithoutException() {
        // given
        StepExecution stepExecution = createStepExecution("settlementStep", BatchStatus.STARTED);

        // when & then
        listener.beforeStep(stepExecution);
    }

    @Test
    @DisplayName("afterStep - Step 성공 시 원래 ExitStatus를 반환한다")
    void afterStep_whenCompleted_shouldReturnOriginalExitStatus() {
        // given
        StepExecution stepExecution = createStepExecution("settlementStep", BatchStatus.COMPLETED);
        stepExecution.setStartTime(LocalDateTime.now().minusSeconds(30));
        stepExecution.setEndTime(LocalDateTime.now());

        // when
        ExitStatus result = listener.afterStep(stepExecution);

        // then
        assertThat(result).isEqualTo(stepExecution.getExitStatus());
    }

    @Test
    @DisplayName("afterStep - Step 실패 시 원래 ExitStatus를 반환한다")
    void afterStep_whenFailed_shouldReturnOriginalExitStatus() {
        // given
        StepExecution stepExecution = createStepExecution("settlementStep", BatchStatus.FAILED);
        stepExecution.setStartTime(LocalDateTime.now().minusSeconds(30));
        stepExecution.setEndTime(LocalDateTime.now());
        stepExecution.addFailureException(new RuntimeException("DB 저장 실패"));

        // when
        ExitStatus result = listener.afterStep(stepExecution);

        // then
        assertThat(result).isEqualTo(stepExecution.getExitStatus());
    }

    @Test
    @DisplayName("afterStep - 실패 예외가 없어도 UNKNOWN으로 예외 없이 실행된다")
    void afterStep_whenFailedWithNoException_shouldFallbackToUnknown() {
        // given
        StepExecution stepExecution = createStepExecution("settlementStep", BatchStatus.FAILED);

        // when
        ExitStatus result = listener.afterStep(stepExecution);

        // then
        assertThat(result).isEqualTo(stepExecution.getExitStatus());
    }

    @Test
    @DisplayName("afterStep - 시작/종료 시간이 없어도 예외 없이 실행된다")
    void afterStep_withoutTimeInfo_shouldRunWithoutException() {
        // given
        StepExecution stepExecution = createStepExecution("settlementStep", BatchStatus.COMPLETED);

        // when
        ExitStatus result = listener.afterStep(stepExecution);

        // then
        assertThat(result).isEqualTo(stepExecution.getExitStatus());
    }

    private StepExecution createStepExecution(String stepName, BatchStatus status) {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
        JobInstance jobInstance = new JobInstance(1L, "settlementJob");
        JobExecution jobExecution = new JobExecution(jobInstance, 1L, params);
        StepExecution stepExecution = new StepExecution(stepName, jobExecution);
        stepExecution.setStatus(status);
        return stepExecution;
    }
}
