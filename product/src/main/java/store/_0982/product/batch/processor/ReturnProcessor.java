package store._0982.product.batch.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.common.dto.ResponseDto;
import store._0982.product.batch.client.OrderClient;
import store._0982.product.domain.GroupPurchase;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnProcessor implements ItemProcessor<GroupPurchase, GroupPurchase> {

    private final OrderClient orderClient;

    @Override
    public GroupPurchase process(GroupPurchase groupPurchase) throws Exception {
        try{
            ResponseDto<Void> response = orderClient.returnOrders(groupPurchase.getGroupPurchaseId());
            log.info("환불 처리 완료: groupPurchaseId={}",
                    groupPurchase.getGroupPurchaseId());
            return groupPurchase;
        }catch (Exception e){
            log.error("환불 실패 : groupPurchaseId={}",groupPurchase.getGroupPurchaseId(),e);
            return null;
        }
    }
}
