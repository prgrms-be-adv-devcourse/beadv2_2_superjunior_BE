package store._0982.commerce.infrastructure.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.order.OrderPaymentProcessorService;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.common.log.ServiceLog;

@RequiredArgsConstructor
@Service
@Slf4j
public class PointChangedKafkaListener {

    private final OrderPaymentProcessorService orderPaymentProcessorService;

    @ServiceLog
    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate",
            exclude = CustomException.class
    )
    @KafkaListener(
            topics = KafkaTopics.POINT_CHANGED,
            groupId = "order-service-group",
            containerFactory = "paymentKafkaListenerFactory"
    )
    public void handlePointChangedEvent(PointChangedEvent event) {
        if (event.getOrderId() == null) {
            log.info("[KAFKA][RECV] topic={} ignored (no orderId). status={}, pointTxId={}",
                    KafkaTopics.POINT_CHANGED, event.getStatus(), event.getTransactionId());
            return;
        }
        log.info("[KAFKA][RECV] topic={}, orderId={}, status={}, pointTxId={}",
                KafkaTopics.POINT_CHANGED, event.getOrderId(), event.getStatus(), event.getTransactionId());
        orderPaymentProcessorService.processPointStatusUpdate(event);
    }

}
