package store._0982.batch.batch.grouppurchase.processor;

import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultWithProductInfo;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseWithProduct;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Component
public class OpenGroupPurchaseProcessor implements ItemProcessor<GroupPurchaseWithProduct, GroupPurchaseResultWithProductInfo> {

    @Override
    public GroupPurchaseResultWithProductInfo process(@NonNull GroupPurchaseWithProduct item) throws Exception {
        // 공동구매 오픈 처리
        GroupPurchase groupPurchase = item.groupPurchase();
        groupPurchase.open();

        return new GroupPurchaseResultWithProductInfo(groupPurchase, item.product(), true);
    }
}
