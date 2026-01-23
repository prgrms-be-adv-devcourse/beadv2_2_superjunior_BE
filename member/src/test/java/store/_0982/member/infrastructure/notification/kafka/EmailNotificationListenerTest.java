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
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.dispatch.EmailDispatchService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmailNotificationListenerTest {

    @Mock
    private EmailDispatchService emailDispatchService;

    @InjectMocks
    private EmailNotificationListener emailNotificationListener;

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
    @DisplayName("주문 생성 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleOrderCreatedEvent_success() {
        String productName = "테스트 상품";
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, memberId, productName);

        emailNotificationListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("주문 취소 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleOrderCanceledEvent_success() {
        String productName = "테스트 상품";
        OrderCanceledEvent event = new OrderCanceledEvent(memberId, orderId, productName,
                "단순 변심", OrderCanceledEvent.PaymentMethod.PG, 10000L);

        emailNotificationListener.handleOrderCanceledEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("주문 확정 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleOrderConfirmedEvent_success() {
        UUID groupPurchaseId = UUID.randomUUID();
        String productName = "테스트 상품";
        OrderConfirmedEvent event = new OrderConfirmedEvent(orderId, memberId, groupPurchaseId,
                productName, OrderConfirmedEvent.ProductCategory.FOOD);

        emailNotificationListener.handleOrderConfirmedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, orderId);
    }

    @Test
    @DisplayName("결제 완료 이벤트는 알림을 발송하지 않는다")
    void handlePaymentChangedEvent_paymentCompleted_noNotification() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.PAYMENT_COMPLETED);

        emailNotificationListener.handlePaymentChangedEvent(event);

        verifyNoInteractions(emailDispatchService);
    }

    @Test
    @DisplayName("결제 실패 이벤트를 처리하고 이메일 알림을 발송한다")
    void handlePaymentChangedEvent_paymentFailed_success() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.PAYMENT_FAILED);

        emailNotificationListener.handlePaymentChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, paymentId);
    }

    @Test
    @DisplayName("결제 환불 이벤트를 처리하고 이메일 알림을 발송한다")
    void handlePaymentChangedEvent_refunded_success() {
        PaymentChangedEvent event = new PaymentChangedEvent(memberId, orderId,
                10000L, paymentId, PaymentChangedEvent.Status.REFUNDED);

        emailNotificationListener.handlePaymentChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, paymentId);
    }

    @Test
    @DisplayName("포인트 사용 이벤트는 알림을 발송하지 않는다")
    void handlePointChangedEvent_used_noNotification() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                5000L, transactionId, PointChangedEvent.Status.USED);

        emailNotificationListener.handlePointChangedEvent(event);

        verifyNoInteractions(emailDispatchService);
    }

    @Test
    @DisplayName("포인트 환불 이벤트를 처리하고 이메일 알림을 발송한다")
    void handlePointChangedEvent_refunded_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                5000L, transactionId, PointChangedEvent.Status.REFUNDED);

        emailNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("포인트 충전 이벤트를 처리하고 이메일 알림을 발송한다")
    void handlePointChangedEvent_charged_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                10000L, transactionId, PointChangedEvent.Status.CHARGED);

        emailNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("포인트 출금 이벤트를 처리하고 이메일 알림을 발송한다")
    void handlePointChangedEvent_withdrawn_success() {
        PointChangedEvent event = new PointChangedEvent(orderId, memberId,
                3000L, transactionId, PointChangedEvent.Status.WITHDRAWN);

        emailNotificationListener.handlePointChangedEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, transactionId);
    }

    @Test
    @DisplayName("정산 완료 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleSettlementDoneEvent_completed_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.COMPLETED,
                100000L, null, BigDecimal.valueOf(100000L));

        emailNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, settlementId);
    }

    @Test
    @DisplayName("정산 실패 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleSettlementDoneEvent_failed_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.FAILED,
                100000L, null, BigDecimal.valueOf(100000L));

        emailNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, settlementId);
    }

    @Test
    @DisplayName("정산 연기 이벤트를 처리하고 이메일 알림을 발송한다")
    void handleSettlementDoneEvent_deferred_success() {
        SettlementDoneEvent event = new SettlementDoneEvent(settlementId, memberId,
                OffsetDateTime.now(), null, SettlementDoneEvent.Status.DEFERRED,
                100000L, null, BigDecimal.valueOf(100000L));

        emailNotificationListener.handleSettlementDoneEvent(event);

        ArgumentCaptor<Notifiable> captor = ArgumentCaptor.forClass(Notifiable.class);
        verify(emailDispatchService).notifyToEmail(captor.capture());

        verifyNotification(captor, settlementId);
    }

    private void verifyNotification(ArgumentCaptor<Notifiable> captor, UUID referenceId) {
        Notifiable captured = captor.getValue();
        assertThat(captured.memberId()).isEqualTo(memberId);
        assertThat(captured.content().referenceId()).isEqualTo(referenceId);
    }
}
