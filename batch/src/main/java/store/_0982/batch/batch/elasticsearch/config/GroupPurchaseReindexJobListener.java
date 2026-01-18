package store._0982.batch.batch.elasticsearch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.util.StringUtils;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class GroupPurchaseReindexJobListener implements JobExecutionListener {

    private static final String INDEX_SUFFIX_PATTERN = "yyyyMMddHHmmss";

    private final GroupPurchaseReindexProperties properties;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        JobParameters params = jobExecution.getJobParameters();
        String alias = getOrDefault(params, "indexAlias", properties.getAlias());
        String mode = getOrDefault(params, "mode", "full");
        OffsetDateTime jobStartTime = OffsetDateTime.now();
        OffsetDateTime since = parseSince(params.getString("since"), jobStartTime);

        String targetIndex = "incremental".equalsIgnoreCase(mode)
                ? alias
                : alias + "-" + jobStartTime.format(DateTimeFormatter.ofPattern(INDEX_SUFFIX_PATTERN));

        ExecutionContext context = jobExecution.getExecutionContext();
        context.putString("indexAlias", alias);
        context.putString("mode", mode);
        context.putString("targetIndex", targetIndex);
        context.putString("since", since.toString());

        log.info(
                BatchLogMessageFormat.jobStart(jobExecution.getJobInstance().getJobName()),
                BatchLogMetadataFormat.jobStart(
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getJobParameters().toString()
                )
        );
    }

    private String getOrDefault(JobParameters params, String key, String defaultValue) {
        String value = params.getString(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private OffsetDateTime parseSince(String raw, OffsetDateTime fallback) {
        if (!StringUtils.hasText(raw)) {
            return fallback;
        }
        return OffsetDateTime.parse(raw);
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
