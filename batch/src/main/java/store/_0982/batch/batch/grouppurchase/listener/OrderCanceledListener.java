package store._0982.batch.batch.grouppurchase.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.batch.grouppurchase.event.OrderCancelProcessedEvent;
import store._0982.batch.domain.order.Order;
import store._0982.batch.domain.order.OrderRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;

@Component
@RequiredArgsConstructor
public class OrderCanceledListener {

    private final KafkaTemplate<String, OrderCanceledEvent> kafkaTemplate;
    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void canceledOrder(OrderCancelProcessedEvent event){
        Order order = orderRepository.findById(event.order().getOrderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        OrderCanceledEvent kafkaEvent = event.order().toEvent(
                event.reason(),
                OrderCanceledEvent.PaymentMethod.valueOf(
                        event.order().getPaymentMethod().name()
                ),
                event.amount()
        );

        kafkaTemplate.send(
                KafkaTopics.ORDER_CANCELED,
                kafkaEvent.getOrderId().toString(),
                kafkaEvent
        );
    }
}
