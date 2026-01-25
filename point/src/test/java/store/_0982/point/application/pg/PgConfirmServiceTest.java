package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgConfirmServiceTest {

    @Mock
    private PgQueryService pgQueryService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PgConfirmService pgConfirmService;

    private UUID memberId;
    private UUID orderId;
    private String paymentKey;
    private long amount;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
        amount = 10000;
    }

    @Test
    @DisplayName("결제 승인 마킹을 성공적으로 처리한다")
    void markConfirmedPayment_success() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, amount, "테스트 공구");
        TossPaymentInfo tossPaymentInfo = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        when(pgQueryService.findCompletablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgConfirmService.markConfirmedPayment(tossPaymentInfo, orderId, memberId);

        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);
        assertThat(pgPayment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(pgPayment.getPaymentKey()).isEqualTo(paymentKey);

        ArgumentCaptor<PaymentConfirmedTxEvent> captor = ArgumentCaptor.forClass(PaymentConfirmedTxEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().pgPayment().getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);
        assertThat(captor.getValue().pgPayment().getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("결제 승인 마킹 시 이벤트가 발행된다")
    void markConfirmedPayment_publishesEvent() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, amount, "테스트 공구");
        TossPaymentInfo tossPaymentInfo = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        when(pgQueryService.findCompletablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgConfirmService.markConfirmedPayment(tossPaymentInfo, orderId, memberId);

        verify(pgQueryService).findCompletablePayment(orderId, memberId);
        verify(applicationEventPublisher).publishEvent(any(PaymentConfirmedTxEvent.class));
    }
}
