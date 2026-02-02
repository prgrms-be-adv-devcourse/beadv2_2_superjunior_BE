package store._0982.member.infrastructure.notification.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.kafka.dto.*;
import store._0982.member.application.member.CommerceQueryPort;
import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.dispatch.InAppDispatchService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InAppNotificationListenerTest {

    @Mock
    private InAppDispatchService inAppDispatchService;

    @Mock
    private CommerceQueryPort commerceQueryPort;

    @InjectMocks
    private InAppNotificationListener inAppNotificationListener;

    private UUID orderId;
    private UUID memberId;
    private UUID paymentId;
    private UUID transactionId;
    private UUID settlementId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        settlementId = UUID.randomUUID();
    }

    @Test
    @DisplayName("주문 생성 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleOrderCreatedEvent_success() {
        String productName = "테스트 상품";
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, memberId, productName);

        inAppNotificationListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("주문 취소 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleOrderCanceledEvent_success() {
        String productName = "테스트 상품";
        OrderCanceledEvent event = new OrderCanceledEvent(memberId, orderId, productName,
                "단순 변심", OrderCanceledEvent.PaymentMethod.PG, 10000L);

        inAppNotificationListener.handleOrderCanceledEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("주문 확정 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleOrderConfirmedEvent_success() {
        UUID groupPurchaseId = UUID.randomUUID();
        String productName = "테스트 상품";
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderId, memberId, groupPurchaseId,
                productName, OrderConfirmedEvent.ProductCategory.FOOD);

        inAppNotificationListener.handleOrderConfirmedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("결제 완료 이벤트는 알림을 발송하지 않는다")
    void handlePaymentChangedEvent_paymentCompleted_noNotification() {
        PaymentChangedEvent event = new PaymentChangedEvent(UUID.randomUUID(), UUID.randomUUID(),
                10000L, UUID.randomUUID(), PaymentChangedEvent.Status.PAYMENT_COMPLETED);

        inAppNotificationListener.handlePaymentChangedEvent(event);

        verifyNoInteractions(inAppDispatchService);
    }

    @Test
    @DisplayName("결제 실패 이벤트를 처리하고 인앱 알림을 발송한다")
    void handlePaymentChangedEvent_paymentFailed_success() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.PAYMENT_FAILED);

        inAppNotificationListener.handlePaymentChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, paymentId);
    }

    @Test
    @DisplayName("결제 환불 이벤트를 처리하고 인앱 알림을 발송한다")
    void handlePaymentChangedEvent_refunded_success() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.REFUNDED);

        inAppNotificationListener.handlePaymentChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, paymentId);
    }

    @Test
    @DisplayName("포인트 사용 이벤트는 알림을 발송하지 않는다")
    void handlePointChangedEvent_used_noNotification() {
        PointChangedEvent event = new PointChangedEvent(UUID.randomUUID(), UUID.randomUUID(),
                5000L, UUID.randomUUID(), PointChangedEvent.Status.USED);

        inAppNotificationListener.handlePointChangedEvent(event);

        verifyNoInteractions(inAppDispatchService);
    }

    @Test
    @DisplayName("포인트 환불 이벤트를 처리하고 인앱 알림을 발송한다")
    void handlePointChangedEvent_refunded_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                5000L, transactionId, PointChangedEvent.Status.REFUNDED);

        inAppNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("포인트 충전 이벤트를 처리하고 인앱 알림을 발송한다")
    void handlePointChangedEvent_charged_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                10000L, transactionId, PointChangedEvent.Status.CHARGED);

        inAppNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("포인트 출금 이벤트를 처리하고 인앱 알림을 발송한다")
    void handlePointChangedEvent_withdrawn_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                3000L, transactionId, PointChangedEvent.Status.WITHDRAWN);

        inAppNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("정산 완료 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleSettlementDoneEvent_completed_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.COMPLETED,
                100000L, null, BigDecimal.valueOf(100000L));

        inAppNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, settlementId);
    }

    @Test
    @DisplayName("정산 실패 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleSettlementDoneEvent_failed_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.FAILED,
                100000L, null, BigDecimal.valueOf(100000L));

        inAppNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, settlementId);
    }

    @Test
    @DisplayName("정산 연기 이벤트를 처리하고 인앱 알림을 발송한다")
    void handleSettlementDoneEvent_deferred_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.DEFERRED,
                100000L, null, BigDecimal.valueOf(100000L));

        inAppNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        verifyNotification(captor, settlementId);
    }

    @Test
    @DisplayName("공동구매 성공 이벤트를 처리하고 참가자들에게 인앱 알림을 발송한다")
    void handleGroupPurchaseChangedEvent_success_sendsToParticipants() {
        UUID groupPurchaseId = UUID.randomUUID();
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

        inAppNotificationListener.handleGroupPurchaseChangedEvent(event);

        verify(commerceQueryPort).getGroupPurchaseParticipants(groupPurchaseId);

        ArgumentCaptor<BulkNotifiable> captor = ArgumentCaptor.forClass(BulkNotifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        BulkNotifiable captured = captor.getValue();
        assertThat(captured.notifiables())
                .extracting(Notifiable::memberId)
                .containsExactlyInAnyOrder(memberId, participant1, participant2, participant3);
        assertThat(captured.notifiables())
                .extracting(notifiable -> notifiable.content().referenceId())
                .containsOnly(groupPurchaseId);
    }

    @Test
    @DisplayName("공동구매 실패 이벤트를 처리하고 참가자들에게 인앱 알림을 발송한다")
    void handleGroupPurchaseChangedEvent_failed_sendsToParticipants() {
        UUID groupPurchaseId = UUID.randomUUID();
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

        inAppNotificationListener.handleGroupPurchaseChangedEvent(event);

        verify(commerceQueryPort).getGroupPurchaseParticipants(groupPurchaseId);

        ArgumentCaptor<BulkNotifiable> captor = ArgumentCaptor.forClass(BulkNotifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        BulkNotifiable captured = captor.getValue();
        assertThat(captured.notifiables())
                .extracting(Notifiable::memberId)
                .containsExactlyInAnyOrder(memberId, participant1, participant2);
        assertThat(captured.notifiables())
                .extracting(notifiable -> notifiable.content().referenceId())
                .containsOnly(groupPurchaseId);
    }

    @Test
    @DisplayName("공동구매 오픈 이벤트는 알림을 발송하지 않는다")
    void handleGroupPurchaseChangedEvent_open_noNotification() {
        GroupPurchaseEvent event = GroupPurchaseEvent.builder()
                .id(UUID.randomUUID())
                .sellerId(memberId)
                .title("공동구매 상품")
                .groupPurchaseStatus(GroupPurchaseEvent.Status.OPEN)
                .build();

        inAppNotificationListener.handleGroupPurchaseChangedEvent(event);

        verifyNoInteractions(commerceQueryPort);
        verifyNoInteractions(inAppDispatchService);
    }

    @Test
    @DisplayName("공동구매 예약 이벤트는 알림을 발송하지 않는다")
    void handleGroupPurchaseChangedEvent_scheduled_noNotification() {
        GroupPurchaseEvent event = GroupPurchaseEvent.builder()
                .id(UUID.randomUUID())
                .sellerId(memberId)
                .title("공동구매 상품")
                .groupPurchaseStatus(GroupPurchaseEvent.Status.SCHEDULED)
                .build();

        inAppNotificationListener.handleGroupPurchaseChangedEvent(event);

        verifyNoInteractions(commerceQueryPort);
        verifyNoInteractions(inAppDispatchService);
    }

    @Test
    @DisplayName("공동구매 참가자가 없는 경우에도 정상적으로 처리된다 (판매자에게만 전송됨)")
    void handleGroupPurchaseChangedEvent_noParticipants_success() {
        UUID groupPurchaseId = UUID.randomUUID();
        String title = "공동구매 상품";
        List<UUID> emptyParticipants = List.of();

        GroupPurchaseEvent event = GroupPurchaseEvent.builder()
                .id(groupPurchaseId)
                .sellerId(memberId)
                .title(title)
                .groupPurchaseStatus(GroupPurchaseEvent.Status.FAILED)
                .build();

        when(commerceQueryPort.getGroupPurchaseParticipants(groupPurchaseId))
                .thenReturn(emptyParticipants);

        inAppNotificationListener.handleGroupPurchaseChangedEvent(event);

        verify(commerceQueryPort).getGroupPurchaseParticipants(groupPurchaseId);

        ArgumentCaptor<BulkNotifiable> captor = ArgumentCaptor.forClass(BulkNotifiable.class);
        verify(inAppDispatchService).notifyToInApp(captor.capture());

        BulkNotifiable captured = captor.getValue();
        assertThat(captured.notifiables())
                .singleElement()
                .extracting(Notifiable::memberId, notifiable -> notifiable.content().referenceId())
                .containsExactly(memberId, groupPurchaseId);
    }

    private void verifyNotification(ArgumentCaptor<Notifiable> captor, UUID referenceId) {
        Notifiable captured = captor.getValue();
        assertThat(captured.memberId()).isEqualTo(memberId);
        assertThat(captured.content().referenceId()).isEqualTo(referenceId);
    }
}
