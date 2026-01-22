package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseWithProduct;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseProcessor implements ItemProcessor<GroupPurchaseWithProduct, GroupPurchaseResultWithProductInfo> {

    @Override
    public GroupPurchaseResultWithProductInfo process(GroupPurchaseWithProduct item) throws Exception {

        GroupPurchase groupPurchase = item.groupPurchase();

        boolean isSuccess = groupPurchase.getCurrentQuantity() >= groupPurchase.getMinQuantity();

        return new GroupPurchaseResultWithProductInfo(groupPurchase, item.product(), isSuccess);
    }
}
