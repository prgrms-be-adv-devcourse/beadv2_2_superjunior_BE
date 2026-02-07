package store._0982.batch.batch.grouppurchase.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OpenGroupPurchaseWriter implements ItemWriter<GroupPurchaseResultProjection> {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(@NonNull Chunk<? extends GroupPurchaseResultProjection> chunk) {
        List<UUID> ids = chunk.getItems().stream()
                .map(GroupPurchaseResultProjection::groupPurchaseId)
                .toList();

        if (!ids.isEmpty()) {
            groupPurchaseRepository.bulkUpdateStatus(ids, GroupPurchaseStatus.OPEN);
        }

        eventPublisher.publishEvent(
                new GroupPurchaseChunkUpdateEvent(List.copyOf(chunk.getItems()))
        );
    }
}
