package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.event.PaymentFailedTxEvent;
import store._0982.point.domain.repository.PgPaymentFailureRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgFailServiceTest {

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PgPaymentFailureRepository pgPaymentFailureRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PgFailService pgFailService;

    private UUID memberId;
    private PgFailCommand command;
    private String paymentKey;

    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
        command = new PgFailCommand(
                orderId,
                paymentKey,
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000L,
                "{}"
        );
    }

    @Test
    @DisplayName("결제 실패 정보를 저장한다")
    void handlePaymentFailure_success() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000L);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when
        pgFailService.handlePaymentFailure(command, memberId);

        // then
        verify(pgPaymentRepository).findByOrderId(orderId);
        verify(pgPaymentFailureRepository).save(any(PgPaymentFailure.class));
        verify(applicationEventPublisher).publishEvent(any(PaymentFailedTxEvent.class));
    }

    @Test
    @DisplayName("이미 성공(COMPLETED)된 결제를 실패 처리하려 하면 예외가 발생한다")
    void handlePaymentFailure_exception_when_completed() {
        // given
        PgPayment completedPayment = PgPayment.create(memberId, orderId, 10000L);
        completedPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

        // when & then
        assertThatThrownBy(() -> pgFailService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());

        verify(pgPaymentFailureRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 실패 처리 요청 시 예외가 발생한다")
    void handlePaymentFailure_not_found() {
        // given
        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pgFailService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());

        verify(pgPaymentFailureRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
