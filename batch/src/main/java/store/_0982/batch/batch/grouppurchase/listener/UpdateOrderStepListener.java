package store._0982.batch.batch.grouppurchase.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderStepListener implements StepExecutionListener {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private Map<UUID, GroupPurchaseStatus> statusList;

    @Value("#{stepExecutionContext['processedGroupPurchaseIds']}")
    private List<UUID> processedGroupPurchaseIds;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution){

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

        List<GroupPurchase> groupPurchases = groupPurchaseRepository.findAllByGroupPurchaseIdIn(processedGroupPurchaseIds);

        this.statusList = groupPurchases.stream()
                .collect(Collectors.toMap(
                        GroupPurchase::getGroupPurchaseId,
                        GroupPurchase::getStatus
                ));

        stepExecution.getExecutionContext()
                .put("groupPurchaseStatusList", statusList);
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
