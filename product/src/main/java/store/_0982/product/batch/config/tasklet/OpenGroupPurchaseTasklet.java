package store._0982.product.batch.config.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import store._0982.product.batch.config.tasklet.event.GroupPurchaseOpenedEvent;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional
public class OpenGroupPurchaseTasklet implements Tasklet {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<GroupPurchase> purchaseList = groupPurchaseRepository.findAllByStatusAndStartDateBefore(
                GroupPurchaseStatus.SCHEDULED, OffsetDateTime.now());

        purchaseList.forEach(gp -> {
            gp.updateStatus(GroupPurchaseStatus.OPEN);
            eventPublisher.publishEvent(new GroupPurchaseOpenedEvent(gp.getGroupPurchaseId()));
        });

        contribution.incrementWriteCount(purchaseList.size());

        return RepeatStatus.FINISHED;
    }
}
