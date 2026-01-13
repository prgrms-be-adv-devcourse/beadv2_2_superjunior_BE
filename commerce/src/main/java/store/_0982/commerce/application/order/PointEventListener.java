package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.common.log.ServiceLog;

@RequiredArgsConstructor
@Service
public class PointEventListener {

    private final OrderRepository orderRepository;

    @ServiceLog
    @Transactional
    @RetryableTopic
    @KafkaListener(topics = KafkaTopics.PAYMENT_CHANGED)
    public void handlePaymentChangedEvent(PaymentChangedEvent event) {
        Order findOrder = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        findOrder.changeStatus(event.getStatus());
    }
}
