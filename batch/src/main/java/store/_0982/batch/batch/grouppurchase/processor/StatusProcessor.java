package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResult;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.infrastructure.client.order.OrderClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusProcessor implements ItemProcessor<GroupPurchase, GroupPurchaseResult> {

    private final OrderClient orderClient;

    @Override
    public GroupPurchaseResult process(GroupPurchase groupPurchase) throws Exception {
        boolean success = groupPurchase.getCurrentQuantity() >= groupPurchase.getMinQuantity();

        log.info("공동구매 처리 판단 groupPurchaseId = {}, success = {}", groupPurchase.getGroupPurchaseId(), success);

        return new GroupPurchaseResult(groupPurchase, success);
    }
}
