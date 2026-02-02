package store._0982.member.infrastructure.notification.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.*;
import store._0982.member.application.member.CommerceQueryPort;
import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.dispatch.EmailDispatchService;
import store._0982.member.application.notification.dispatch.InAppDispatchService;
import store._0982.member.support.BaseIntegrationTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationKafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @MockitoBean
    private EmailDispatchService emailDispatchService;

    @MockitoBean
    private InAppDispatchService inAppDispatchService;

    @MockitoBean
    private CommerceQueryPort commerceQueryPort;

    private UUID orderId;
    private UUID memberId;
    private UUID paymentId;
    private UUID transactionId;
    private UUID settlementId;
    private UUID groupPurchaseId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        settlementId = UUID.randomUUID();
        groupPurchaseId = UUID.randomUUID();
        reset(emailDispatchService, inAppDispatchService, commerceQueryPort);
    }

    @Test
    @DisplayName("주문 생성 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleOrderCreatedEvent_sendsEmailAndInAppNotification() {
        String productName = "테스트 상품";
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, memberId, productName);

        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, orderId);
        verifyNotification(inAppCaptor, memberId, orderId);
    }

    @Test
    @DisplayName("주문 취소 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleOrderCanceledEvent_sendsEmailAndInAppNotification() {
        String productName = "테스트 상품";
        OrderCanceledEvent event = new OrderCanceledEvent(memberId, orderId, productName,
                "단순 변심", OrderCanceledEvent.PaymentMethod.PG, 10000L);

        kafkaTemplate.send(KafkaTopics.ORDER_CANCELED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, orderId);
        verifyNotification(inAppCaptor, memberId, orderId);
    }

    @Test
    @DisplayName("주문 확정 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleOrderConfirmedEvent_sendsEmailAndInAppNotification() {
        String productName = "테스트 상품";
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderId, memberId, groupPurchaseId,
                productName, OrderConfirmedEvent.ProductCategory.FOOD);

        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, orderId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, orderId);
        verifyNotification(inAppCaptor, memberId, orderId);
    }

    @Test
    @DisplayName("결제 실패 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handlePaymentFailedEvent_sendsEmailAndInAppNotification() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.PAYMENT_FAILED);

        kafkaTemplate.send(KafkaTopics.PAYMENT_CHANGED, paymentId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, paymentId);
        verifyNotification(inAppCaptor, memberId, paymentId);
    }

    @Test
    @DisplayName("결제 환불 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handlePaymentRefundedEvent_sendsEmailAndInAppNotification() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.REFUNDED);

        kafkaTemplate.send(KafkaTopics.PAYMENT_CHANGED, paymentId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, paymentId);
        verifyNotification(inAppCaptor, memberId, paymentId);
    }

    @Test
    @DisplayName("포인트 충전 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handlePointChargedEvent_sendsEmailAndInAppNotification() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                10000L, transactionId, PointChangedEvent.Status.CHARGED);

        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, transactionId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, transactionId);
        verifyNotification(inAppCaptor, memberId, transactionId);
    }

    @Test
    @DisplayName("포인트 환불 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handlePointRefundedEvent_sendsEmailAndInAppNotification() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                5000L, transactionId, PointChangedEvent.Status.REFUNDED);

        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, transactionId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, transactionId);
        verifyNotification(inAppCaptor, memberId, transactionId);
    }

    @Test
    @DisplayName("포인트 출금 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handlePointWithdrawnEvent_sendsEmailAndInAppNotification() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                3000L, transactionId, PointChangedEvent.Status.WITHDRAWN);

        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, transactionId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, transactionId);
        verifyNotification(inAppCaptor, memberId, transactionId);
    }

    @Test
    @DisplayName("정산 완료 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleSettlementCompletedEvent_sendsEmailAndInAppNotification() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.COMPLETED,
                100000L, null, BigDecimal.valueOf(100000L));

        kafkaTemplate.send(KafkaTopics.SETTLEMENT_DONE, settlementId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, settlementId);
        verifyNotification(inAppCaptor, memberId, settlementId);
    }

    @Test
    @DisplayName("정산 실패 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleSettlementFailedEvent_sendsEmailAndInAppNotification() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.FAILED,
                100000L, null, BigDecimal.valueOf(100000L));

        kafkaTemplate.send(KafkaTopics.SETTLEMENT_DONE, settlementId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, settlementId);
        verifyNotification(inAppCaptor, memberId, settlementId);
    }

    @Test
    @DisplayName("정산 연기 이벤트를 Kafka로 발행하면 이메일과 인앱 알림이 발송된다")
    void handleSettlementDeferredEvent_sendsEmailAndInAppNotification() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.DEFERRED,
                100000L, null, BigDecimal.valueOf(100000L));

        kafkaTemplate.send(KafkaTopics.SETTLEMENT_DONE, settlementId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    verify(emailDispatchService).notifyToEmail(any(Notifiable.class));
                    verify(inAppDispatchService).notifyToInApp(any(Notifiable.class));
                });

        ArgumentCaptor<Notifiable> emailCaptor = ArgumentCaptor.forClass(Notifiable.class);
        ArgumentCaptor<Notifiable> inAppCaptor = ArgumentCaptor.forClass(Notifiable.class);

        verify(emailDispatchService).notifyToEmail(emailCaptor.capture());
        verify(inAppDispatchService).notifyToInApp(inAppCaptor.capture());

        verifyNotification(emailCaptor, memberId, settlementId);
        verifyNotification(inAppCaptor, memberId, settlementId);
    }

    @Test
    @DisplayName("공동구매 성공 이벤트를 Kafka로 발행하면 판매자와 참가자들에게 인앱 알림이 발송된다")
    void handleGroupPurchaseSuccessEvent_sendsInAppNotificationToSellerAndParticipants() {
        String title = "공동구매 상품";
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        UUID participant3 = UUID.randomUUID();
        List<UUID> participants = List.of(participant1, participant2, participant3);

        GroupPurchaseEvent event = GroupPurchaseEvent.builder()
                .id(groupPurchaseId)
                .sellerId(memberId)
                .title(title)
                .groupPurchaseStatus(GroupPurchaseEvent.Status.SUCCESS)
                .build();

        when(commerceQueryPort.getGroupPurchaseParticipants(groupPurchaseId))
                .thenReturn(participants);

        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, groupPurchaseId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(inAppDispatchService).notifyToInApp(any(BulkNotifiable.class)));

        ArgumentCaptor<BulkNotifiable> captor = ArgumentCaptor.forClass(BulkNotifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        BulkNotifiable captured = captor.getValue();
        assertThat(captured.notifiables())
                .extracting(Notifiable::memberId)
                .containsExactlyInAnyOrder(memberId, participant1, participant2, participant3);
        assertThat(captured.notifiables())
                .extracting(notifiable -> notifiable.content().referenceId())
                .containsOnly(groupPurchaseId);

        verifyNoInteractions(emailDispatchService);
    }

    @Test
    @DisplayName("공동구매 실패 이벤트를 Kafka로 발행하면 판매자와 참가자들에게 인앱 알림이 발송된다")
    void handleGroupPurchaseFailedEvent_sendsInAppNotificationToSellerAndParticipants() {
        String title = "공동구매 상품";
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();
        List<UUID> participants = List.of(participant1, participant2);

        GroupPurchaseEvent event = GroupPurchaseEvent.builder()
                .id(groupPurchaseId)
                .sellerId(memberId)
                .title(title)
                .groupPurchaseStatus(GroupPurchaseEvent.Status.FAILED)
                .build();

        when(commerceQueryPort.getGroupPurchaseParticipants(groupPurchaseId))
                .thenReturn(participants);

        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, groupPurchaseId.toString(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(inAppDispatchService).notifyToInApp(any(BulkNotifiable.class)));

        ArgumentCaptor<BulkNotifiable> captor = ArgumentCaptor.forClass(BulkNotifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        BulkNotifiable captured = captor.getValue();
        assertThat(captured.notifiables())
                .extracting(Notifiable::memberId)
                .containsExactlyInAnyOrder(memberId, participant1, participant2);
        assertThat(captured.notifiables())
                .extracting(notifiable -> notifiable.content().referenceId())
                .containsOnly(groupPurchaseId);

        verifyNoInteractions(emailDispatchService);
    }

    private void verifyNotification(ArgumentCaptor<Notifiable> captor, UUID expectedMemberId, UUID expectedReferenceId) {
        Notifiable captured = captor.getValue();
        assertThat(captured.memberId()).isEqualTo(expectedMemberId);
        assertThat(captured.content().referenceId()).isEqualTo(expectedReferenceId);
    }
}
