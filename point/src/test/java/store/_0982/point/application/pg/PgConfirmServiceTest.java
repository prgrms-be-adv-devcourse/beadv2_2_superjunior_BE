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
import store._0982.common.exception.CustomException;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgConfirmServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private OrderQueryService orderQueryService;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PgCommandManager pgCommandManager;

    @InjectMocks
    private PgReadManager pgReadManager;

    private PgConfirmService pgConfirmService;

    private UUID memberId;
    private UUID orderId;
    private String paymentKey;

    @BeforeEach
    void setUp() {
        pgConfirmService = new PgConfirmService(tossPaymentService, pgReadManager, pgCommandManager, orderQueryService);

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
    }

    @Test
    @DisplayName("결제 승인을 완료하고 포인트를 충전한다")
    void confirmPayment_success() {
        // given
        int amount = 10000;
        PgPayment pgPayment = PgPayment.create(memberId, orderId, amount);
        PgConfirmCommand command = new PgConfirmCommand(orderId, amount, paymentKey);

        TossPaymentInfo tossResponse = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
        doNothing().when(orderQueryService).validateOrderPayable(memberId, orderId, amount);
        when(tossPaymentService.confirmPayment(any(), any())).thenReturn(tossResponse);
        doNothing().when(applicationEventPublisher).publishEvent(any(PaymentConfirmedTxEvent.class));

        // when
        pgConfirmService.confirmPayment(command, memberId);

        // then
        verify(pgPaymentRepository, times(2)).findByOrderId(orderId);
        verify(orderQueryService).validateOrderPayable(memberId, orderId, amount);
        verify(tossPaymentService).confirmPayment(any(), any());

        ArgumentCaptor<PaymentConfirmedTxEvent> captor = ArgumentCaptor.forClass(PaymentConfirmedTxEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().pgPayment().getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);
        assertThat(captor.getValue().pgPayment().getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 승인 요청 시 예외가 발생한다")
    void confirmPayment_fail_whenPaymentNotFound() {
        // given
        PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pgConfirmService.confirmPayment(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("이미 승인된 주문으로 재승인 시 예외가 발생한다")
    void confirmPayment_fail_whenAlreadyCompleted() {
        // given
        PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);
        PgPayment completedPayment = PgPayment.create(memberId, orderId, 10000);
        completedPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

        // when & then
        assertThatThrownBy(() -> pgConfirmService.confirmPayment(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
