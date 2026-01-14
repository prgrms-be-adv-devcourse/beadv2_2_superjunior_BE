package store._0982.batch.batch.elasticsearch.reindex.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.elasticsearch.reindex.service.GroupPurchaseReindexService;

@Component
@RequiredArgsConstructor
public class CreateGroupPurchaseIndexTasklet implements Tasklet {

    private final GroupPurchaseReindexService reindexService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        ExecutionContext context = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        String mode = context.getString("mode", "full");
        if ("incremental".equalsIgnoreCase(mode)) {
            return RepeatStatus.FINISHED;
        }
        String targetIndex = context.getString("targetIndex");
        reindexService.createIndexWithMapping(targetIndex);
        return RepeatStatus.FINISHED;
    }
}
