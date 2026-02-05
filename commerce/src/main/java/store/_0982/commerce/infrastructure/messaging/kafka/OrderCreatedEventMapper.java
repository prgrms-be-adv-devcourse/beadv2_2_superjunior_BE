package store._0982.commerce.infrastructure.messaging.kafka;

import org.springframework.stereotype.Component;
import store._0982.commerce.domain.order.Order;
import store._0982.common.kafka.dto.OrderCreatedEvent;

@Component
public class OrderCreatedEventMapper {

    public OrderCreatedEvent toMessage(Order order, String productName){
        return new OrderCreatedEvent(
                order.getOrderId(),
                order.getMemberId(),
                productName
        );
    }
}
