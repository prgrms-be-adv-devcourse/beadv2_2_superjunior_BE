package store._0982.batch.batch.grouppurchase.processor;

import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Component
public class OpenGroupPurchaseProcessor implements ItemProcessor<GroupPurchase, GroupPurchase> {

    @Nullable
    @Override
    public GroupPurchase process(@NonNull GroupPurchase groupPurchase) throws Exception {
        groupPurchase.open();
        return groupPurchase;
    }
}
