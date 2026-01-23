package store._0982.member.infrastructure.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.*;
import store._0982.member.application.member.CommerceQueryPort;
import store._0982.member.application.notification.dispatch.InAppDispatchService;
import store._0982.member.application.notification.dto.kafka.group_purchase.GroupPurchaseFailedCommand;
import store._0982.member.application.notification.dto.kafka.group_purchase.GroupPurchaseSuccessCommand;
import store._0982.member.application.notification.dto.kafka.order.OrderCanceledCommand;
import store._0982.member.application.notification.dto.kafka.order.OrderCompletedCommand;
import store._0982.member.application.notification.dto.kafka.order.OrderConfirmedCommand;
import store._0982.member.application.notification.dto.kafka.payment.PaymentFailedCommand;
import store._0982.member.application.notification.dto.kafka.payment.PaymentRefundedCommand;
import store._0982.member.application.notification.dto.kafka.point.PointChargedCommand;
import store._0982.member.application.notification.dto.kafka.point.PointRefundedCommand;
import store._0982.member.application.notification.dto.kafka.point.PointWithdrawnCommand;
import store._0982.member.application.notification.dto.kafka.settlement.SettlementCompletedCommand;
import store._0982.member.application.notification.dto.kafka.settlement.SettlementDeferredCommand;
import store._0982.member.application.notification.dto.kafka.settlement.SettlementFailedCommand;
import store._0982.member.common.notification.CustomRetryableTopic;
import store._0982.member.common.notification.InAppKafkaListener;
import store._0982.member.exception.NegligibleKafkaErrorType;
import store._0982.member.exception.NegligibleKafkaException;

@Component
@RequiredArgsConstructor
public class InAppNotificationListener {

    private final InAppDispatchService inAppDispatchService;
    private final CommerceQueryPort commerceQueryPort;

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CREATED)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        inAppDispatchService.notifyToInApp(OrderCompletedCommand.from(event));
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CANCELED)
    public void handleOrderCanceledEvent(OrderCanceledEvent event) {
        inAppDispatchService.notifyToInApp(OrderCanceledCommand.from(event));
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CONFIRMED)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        inAppDispatchService.notifyToInApp(OrderConfirmedCommand.from(event));
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.PAYMENT_CHANGED)
    public void handlePaymentChangedEvent(PaymentChangedEvent event) {
        switch (event.getStatus()) {
            case PAYMENT_COMPLETED -> {
                // 주문 완료 알림이 이를 대신함
            }
            case PAYMENT_FAILED -> inAppDispatchService.notifyToInApp(PaymentFailedCommand.from(event));
            case REFUNDED -> inAppDispatchService.notifyToInApp(PaymentRefundedCommand.from(event));
            default -> throw new NegligibleKafkaException(NegligibleKafkaErrorType.KAFKA_INVALID_EVENT);
        }
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.POINT_CHANGED)
    public void handlePointChangedEvent(PointChangedEvent event) {
        switch (event.getStatus()) {
            case USED -> {
                // 주문 완료 알림이 이를 대신함
            }
            case REFUNDED -> inAppDispatchService.notifyToInApp(PointRefundedCommand.from(event));
            case CHARGED -> inAppDispatchService.notifyToInApp(PointChargedCommand.from(event));
            case WITHDRAWN -> inAppDispatchService.notifyToInApp(PointWithdrawnCommand.from(event));
            default -> throw new NegligibleKafkaException(NegligibleKafkaErrorType.KAFKA_INVALID_EVENT);
        }
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.SETTLEMENT_DONE)
    public void handleSettlementDoneEvent(SettlementDoneEvent event) {
        switch (event.getStatus()) {
            case COMPLETED -> inAppDispatchService.notifyToInApp(SettlementCompletedCommand.from(event));
            case FAILED -> inAppDispatchService.notifyToInApp(SettlementFailedCommand.from(event));
            case DEFERRED -> inAppDispatchService.notifyToInApp(SettlementDeferredCommand.from(event));
            default -> throw new NegligibleKafkaException(NegligibleKafkaErrorType.KAFKA_INVALID_EVENT);
        }
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.GROUP_PURCHASE_CHANGED)
    public void handleGroupPurchaseChangedEvent(GroupPurchaseEvent event) {
        switch (event.getGroupPurchaseStatus()) {
            case SUCCESS -> inAppDispatchService.notifyToInApp(
                    GroupPurchaseSuccessCommand.of(
                            event.getId(),
                            event.getSellerId(),
                            event.getTitle(),
                            commerceQueryPort.getGroupPurchaseParticipants(event.getId())
                    )
            );
            case FAILED -> inAppDispatchService.notifyToInApp(
                    GroupPurchaseFailedCommand.of(
                            event.getId(),
                            event.getSellerId(),
                            event.getTitle(),
                            commerceQueryPort.getGroupPurchaseParticipants(event.getId())
                    )
            );
            case OPEN, SCHEDULED -> {
                // 무시
            }
            default -> throw new NegligibleKafkaException(NegligibleKafkaErrorType.KAFKA_INVALID_EVENT);
        }
    }
}
