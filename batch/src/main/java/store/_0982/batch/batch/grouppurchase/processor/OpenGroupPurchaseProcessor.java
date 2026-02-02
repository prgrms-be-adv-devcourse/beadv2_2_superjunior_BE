package store._0982.batch.batch.grouppurchase.processor;

import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

@Component
public class OpenGroupPurchaseProcessor implements ItemProcessor<GroupPurchaseProjection, GroupPurchaseResultProjection> {

    @Override
    public GroupPurchaseResultProjection process(@NonNull GroupPurchaseProjection item) throws Exception {

        return GroupPurchaseResultProjection.from(item, GroupPurchaseStatus.OPEN, true);
    }
}
