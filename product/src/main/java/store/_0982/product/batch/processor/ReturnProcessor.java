package store._0982.product.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.product.batch.dto.GroupPurchaseResult;
import store._0982.product.client.OrderClient;
import store._0982.product.domain.grouppurchase.GroupPurchase;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnProcessor implements ItemProcessor<GroupPurchase, GroupPurchaseResult> {

    private final OrderClient orderClient;

    @Override
    public GroupPurchaseResult process(GroupPurchase groupPurchase) throws Exception {
        try{
            orderClient.returnOrders(groupPurchase.getGroupPurchaseId());
            log.info("환불 처리 완료: groupPurchaseId={}", groupPurchase.getGroupPurchaseId());
            return new GroupPurchaseResult(groupPurchase, true);
        }catch (Exception e){
            log.error("환불 실패 : groupPurchaseId={}",groupPurchase.getGroupPurchaseId(),e);
            return new GroupPurchaseResult(groupPurchase, false);
        }
    }
}
