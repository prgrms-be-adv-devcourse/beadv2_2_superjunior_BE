package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseProjection;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseProcessor implements ItemProcessor<GroupPurchaseProjection, GroupPurchaseResultProjection> {

    @Override
    public GroupPurchaseResultProjection process(GroupPurchaseProjection item) throws Exception {
        boolean isSuccess = item.currentQuantity() >= item.minQuantity();
        GroupPurchaseStatus targetStatus = isSuccess ? GroupPurchaseStatus.SUCCESS : GroupPurchaseStatus.FAILED;

        return GroupPurchaseResultProjection.from(item, targetStatus, isSuccess);
    }
}
