package store._0982.batch.batch.grouppurchase.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseChunkUpdateEvent;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenGroupPurchaseWriter implements ItemWriter<GroupPurchaseResultWithProductInfo> {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(@NonNull Chunk<? extends GroupPurchaseResultWithProductInfo> chunk){
        List<GroupPurchase> toUpdate = chunk.getItems().stream()
                        .map(GroupPurchaseResultWithProductInfo::groupPurchase)
                        .collect(Collectors.toList());
        if(!toUpdate.isEmpty()){
            groupPurchaseRepository.saveAll(toUpdate);
        }

        List<GroupPurchaseResultWithProductInfo> eventItems = new ArrayList<>(chunk.getItems());
        eventPublisher.publishEvent(
                new GroupPurchaseChunkUpdateEvent(eventItems)
        );
    }
}
