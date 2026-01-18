package store._0982.batch.batch.grouppurchase.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResult;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseUpdatedEvent;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseWriter implements ItemWriter<GroupPurchaseResult> {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends GroupPurchaseResult> chunk) throws Exception {
        List<GroupPurchase> toUpdate = new ArrayList<>();
        List<UUID> processedIds = new ArrayList<>();

        for(GroupPurchaseResult result : chunk.getItems()){
            GroupPurchase groupPurchase = result.groupPurchase();
            if(result.success()) groupPurchase.markSuccess();
            else groupPurchase.markFailed();

            toUpdate.add(groupPurchase);
            processedIds.add(groupPurchase.getGroupPurchaseId());
        }
        groupPurchaseRepository.saveAll(toUpdate);

        for(GroupPurchase groupPurchase : toUpdate){
            eventPublisher.publishEvent(new GroupPurchaseUpdatedEvent(groupPurchase.getGroupPurchaseId()));
        }

        // StepExecutionContext에 Id 저장
        StepExecution stepExecution = Objects.requireNonNull(StepSynchronizationManager.getContext()).getStepExecution();
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        @SuppressWarnings("unchecked")
        List<UUID> allProcessedIds = (List<UUID>) executionContext.get("processedGroupPurchaseIds");

        if(allProcessedIds == null){
            allProcessedIds = new ArrayList<>();
        }

        executionContext.put("processedGroupPurchaseIds", allProcessedIds);
    }
}
