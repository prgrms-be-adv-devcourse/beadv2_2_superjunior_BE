package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResult;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Component
@RequiredArgsConstructor
public class UpdateStatusClosedGroupPurchaseProcessor implements ItemProcessor<GroupPurchase, GroupPurchaseResult> {

    @Override
    public GroupPurchaseResult process(GroupPurchase groupPurchase) throws Exception {
        boolean isSuccess = groupPurchase.getCurrentQuantity() >= groupPurchase.getMinQuantity();

        return new GroupPurchaseResult(groupPurchase, isSuccess);
    }
}
