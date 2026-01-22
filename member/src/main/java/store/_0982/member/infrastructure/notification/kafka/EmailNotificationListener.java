package store._0982.member.infrastructure.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.common.kafka.dto.OrderCreatedEvent;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.member.application.notification.dispatch.EmailDispatchService;
import store._0982.member.application.notification.dto.*;
import store._0982.member.common.notification.CustomRetryableTopic;
import store._0982.member.common.notification.EmailKafkaListener;
import store._0982.member.exception.NegligibleKafkaErrorType;
import store._0982.member.exception.NegligibleKafkaException;

@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    private final EmailDispatchService emailDispatchService;

    @CustomRetryableTopic
    @EmailKafkaListener(KafkaTopics.ORDER_CREATED)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        emailDispatchService.notifyToEmail(OrderCompletedCommand.from(event));
    }

    @CustomRetryableTopic
    @EmailKafkaListener(KafkaTopics.ORDER_CANCELED)
    public void handleOrderCanceledEvent(OrderCanceledEvent event) {
        emailDispatchService.notifyToEmail(OrderCanceledCommand.from(event));
    }

    @CustomRetryableTopic
    @EmailKafkaListener(KafkaTopics.ORDER_CONFIRMED)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        emailDispatchService.notifyToEmail(OrderConfirmedCommand.from(event));
    }

    @CustomRetryableTopic
    @EmailKafkaListener(KafkaTopics.PAYMENT_CHANGED)
    public void handlePaymentChangedEvent(PaymentChangedEvent event) {
        switch (event.getStatus()) {
            case PAYMENT_COMPLETED -> {
                // 주문 완료 알림이 이를 대신함
            }
            case PAYMENT_FAILED -> emailDispatchService.notifyToEmail(PaymentFailedCommand.from(event));
            case REFUNDED -> emailDispatchService.notifyToEmail(PaymentRefundedCommand.from(event));
            default -> throw new NegligibleKafkaException(NegligibleKafkaErrorType.KAFKA_INVALID_EVENT);
        }
    }
}
