package store._0982.batch.batch.grouppurchase.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenGroupPurchaseWriter implements ItemWriter<GroupPurchase> {

    private final GroupPurchaseRepository groupPurchaseRepository;

    @Override
    public void write(@NonNull Chunk<? extends GroupPurchase> chunk){
        List<GroupPurchase> items = new ArrayList<>(chunk.getItems());
        groupPurchaseRepository.saveAll(items);
    }
}
