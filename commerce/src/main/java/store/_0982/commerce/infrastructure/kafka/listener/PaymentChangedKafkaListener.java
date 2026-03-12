package store._0982.commerce.infrastructure.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.order.OrderPaymentProcessorService;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.common.log.ServiceLog;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentChangedKafkaListener {

    private final OrderPaymentProcessorService orderPaymentProcessorService;

    @ServiceLog
    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate",
            exclude = CustomException.class
    )
    @KafkaListener(
            topics = KafkaTopics.PAYMENT_CHANGED,
            groupId = "order-service-group",
            containerFactory = "paymentKafkaListenerFactory"
    )
    public void handlePaymentChangedEvent(PaymentChangedEvent event) {
        log.info("[KAFKA][RECV] topic={}, orderId={}, status={}, paymentId={}",
                KafkaTopics.PAYMENT_CHANGED, event.getOrderId(), event.getStatus(), event.getPaymentId());
        orderPaymentProcessorService.processPaymentStatusUpdate(event);
    }
}
