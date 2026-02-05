package store._0982.batch.batch.elasticsearch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.elasticsearch.config.GroupPurchaseReindexProperties;
import store._0982.batch.application.elasticsearch.GroupPurchaseReindexService;

@Component
@RequiredArgsConstructor
public class GroupPurchaseAliasSwitchTasklet implements Tasklet {

    private final GroupPurchaseReindexService reindexService;
    private final GroupPurchaseReindexProperties properties;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        ExecutionContext context = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        String mode = context.getString("mode", "full");
        if (!properties.isSwitchAlias() || "incremental".equalsIgnoreCase(mode)) {
            return RepeatStatus.FINISHED;
        }
        String alias = context.getString("indexAlias");
        String targetIndex = context.getString("targetIndex");
        reindexService.switchAliasAfterValidation(alias, targetIndex);
        return RepeatStatus.FINISHED;
    }
}
