package store._0982.commerce.infrastructure.kafka.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.OrderPaymentProcessorService;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.common.log.ServiceLog;

@RequiredArgsConstructor
@Service
public class PaymentChangedKafkaListener {

    private final OrderPaymentProcessorService orderPaymentProcessorService;

    @ServiceLog
    @Transactional
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
        orderPaymentProcessorService.processPaymentStatusUpdate(event);
    }
}
