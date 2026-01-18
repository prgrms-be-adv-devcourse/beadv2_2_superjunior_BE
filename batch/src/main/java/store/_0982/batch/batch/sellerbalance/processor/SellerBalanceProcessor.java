package store._0982.batch.batch.sellerbalance.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchase;

@Component
public class SellerBalanceProcessor implements ItemProcessor<GroupPurchase, GroupPurchase> {

    @Override
    public GroupPurchase process(GroupPurchase groupPurchase) {
        return groupPurchase;
    }
}
