package store._0982.product.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.product.client.OrderClient;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusProcessor implements ItemProcessor<GroupPurchase, GroupPurchase> {

    private final OrderClient orderClient;

    @Override
    public GroupPurchase process(GroupPurchase groupPurchase) throws Exception {
        int currentQuantity = groupPurchase.getCurrentQuantity();
        int minQuantity = groupPurchase.getMinQuantity();

        if(currentQuantity >= minQuantity){
            groupPurchase.updateStatus(GroupPurchaseStatus.SUCCESS);
            log.info("공동구매 성공 {}", groupPurchase.getGroupPurchaseId());
        }else{
            groupPurchase.updateStatus(GroupPurchaseStatus.FAILED);
            log.info("공동구매 실패 {}", groupPurchase.getGroupPurchaseId());
        }
        return groupPurchase;
    }
}
