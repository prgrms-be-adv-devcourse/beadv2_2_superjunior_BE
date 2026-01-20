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
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkFailedEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseFailedProcessedEvent;
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
        List<GroupPurchase> failedGroupPurchases = new ArrayList<>();

        for(GroupPurchaseResult result : chunk.getItems()){
            GroupPurchase groupPurchase = result.groupPurchase();
            if(result.success()){
                groupPurchase.markSuccess();
            }
            else{
                groupPurchase.markFailed();
                failedGroupPurchases.add(groupPurchase);
            }

            toUpdate.add(groupPurchase);
        }
        groupPurchaseRepository.saveAll(toUpdate);

        if(!failedGroupPurchases.isEmpty()){
            eventPublisher.publishEvent(
                    new GroupPurchaseChunkFailedEvent(failedGroupPurchases));
        }

    }
}
