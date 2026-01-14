package store._0982.batch.batch.elasticsearch.reindex.config;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.JobParameters;
import org.springframework.util.StringUtils;

public class GroupPurchaseReindexModeDecider implements JobExecutionDecider {

    static final String STATUS_FULL = "FULL";
    static final String STATUS_INCREMENTAL = "INCREMENTAL";

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        JobParameters params = jobExecution.getJobParameters();
        String mode = params.getString("mode");
        if (!StringUtils.hasText(mode)) {
            return new FlowExecutionStatus(STATUS_FULL);
        }
        if ("incremental".equalsIgnoreCase(mode)) {
            return new FlowExecutionStatus(STATUS_INCREMENTAL);
        }
        return new FlowExecutionStatus(STATUS_FULL);
    }
}
