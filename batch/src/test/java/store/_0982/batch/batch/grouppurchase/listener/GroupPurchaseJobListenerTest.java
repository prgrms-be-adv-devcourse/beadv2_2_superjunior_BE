package store._0982.batch.batch.grouppurchase.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.*;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupPurchaseJobListener 단위 테스트")
class GroupPurchaseJobListenerTest {

    private GroupPurchaseJobListener listener;

    @BeforeEach
    void setUp() {
        listener = new GroupPurchaseJobListener();
    }

    @Test
    @DisplayName("beforeJob 호출 시 로그가 기록된다")
    void beforeJob_shouldLogJobStart() {
        // given
        JobExecution jobExecution = createJobExecution("groupPurchaseJob", BatchStatus.STARTED);

        // when & then (로깅 동작 확인 - 예외 없이 실행되면 성공)
        listener.beforeJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - 성공 시 로그가 기록된다")
    void afterJob_whenSuccess_shouldLogSuccess() {
        // given
        JobExecution jobExecution = createJobExecution("groupPurchaseJob", BatchStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.now().minusMinutes(1));
        jobExecution.setEndTime(LocalDateTime.now());

        // when & then (로깅 동작 확인 - 예외 없이 실행되면 성공)
        listener.afterJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - 실패 시 에러 로그가 기록된다")
    void afterJob_whenFailed_shouldLogError() {
        // given
        JobExecution jobExecution = createJobExecution("groupPurchaseJob", BatchStatus.FAILED);
        jobExecution.setStartTime(LocalDateTime.now().minusMinutes(1));
        jobExecution.setEndTime(LocalDateTime.now());
        jobExecution.addFailureException(new RuntimeException("Test error"));

        StepExecution stepExecution = new StepExecution("openGroupPurchaseStep", jobExecution);
        stepExecution.setStatus(BatchStatus.FAILED);
        jobExecution.addStepExecutions(Collections.singletonList(stepExecution));

        // when & then (로깅 동작 확인 - 예외 없이 실행되면 성공)
        listener.afterJob(jobExecution);
    }

    @Test
    @DisplayName("afterJob - 시간 정보가 없어도 정상 동작한다")
    void afterJob_withoutTimeInfo_shouldWork() {
        // given
        JobExecution jobExecution = createJobExecution("groupPurchaseJob", BatchStatus.COMPLETED);

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
