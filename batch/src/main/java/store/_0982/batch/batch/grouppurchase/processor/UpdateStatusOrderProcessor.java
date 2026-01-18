package store._0982.batch.batch.grouppurchase.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.batch.domain.order.Order;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@StepScope
public class UpdateStatusOrderProcessor implements ItemProcessor<Order, Order> {

    @Value("#{stepExecutionContext['groupPurchaseStatusList']}")
    private Map<UUID, GroupPurchaseStatus> statusList;

    @Override
    public Order process(Order order) throws Exception{
        GroupPurchaseStatus newStatus = statusList.get(order.getGroupPurchaseId());
        if (newStatus == GroupPurchaseStatus.SUCCESS) {
            order.markGroupPurchaseSuccess();
        } else if (newStatus == GroupPurchaseStatus.FAILED) {
            order.markGroupPurchaseFail();
        }
        return order;
    }
}
