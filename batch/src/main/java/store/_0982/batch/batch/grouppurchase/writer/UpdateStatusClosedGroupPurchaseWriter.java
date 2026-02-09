package store._0982.batch.batch.grouppurchase.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkFailedEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseWriter implements ItemWriter<GroupPurchaseResultProjection> {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends GroupPurchaseResultProjection> chunk) {
        List<GroupPurchaseResultProjection> items = new ArrayList<>(chunk.getItems());

        List<GroupPurchaseResultProjection> successItems = items.stream()
                .filter(GroupPurchaseResultProjection::success)
                .toList();

        List<GroupPurchaseResultProjection> failedItems = items.stream()
                .filter(item -> !item.success())
                .toList();

        if (!successItems.isEmpty()) {
            List<UUID> successIds = successItems.stream()
                    .map(GroupPurchaseResultProjection::groupPurchaseId)
                    .toList();
            groupPurchaseRepository.bulkUpdateStatusWithSucceededAt(successIds, GroupPurchaseStatus.SUCCESS);
        }


        if (!failedItems.isEmpty()) {
            List<UUID> failedIds = failedItems.stream()
                    .map(GroupPurchaseResultProjection::groupPurchaseId)
                    .toList();
            groupPurchaseRepository.bulkUpdateStatus(failedIds, GroupPurchaseStatus.FAILED);

            eventPublisher.publishEvent(new GroupPurchaseChunkFailedEvent(failedItems));
        }

        eventPublisher.publishEvent(new GroupPurchaseChunkUpdateEvent(items));

    }
}
