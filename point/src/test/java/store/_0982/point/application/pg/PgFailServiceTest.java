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
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.event.PaymentFailedTxEvent;
import store._0982.point.domain.repository.PgPaymentFailureRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgFailServiceTest {

    private static final String SAMPLE_PURCHASE_NAME = "테스트 공구";
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final long AMOUNT = 10000;

    @Mock
    private PgPaymentFailureRepository pgPaymentFailureRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PgQueryService pgQueryService;

    @InjectMocks
    private PgFailService pgFailService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("PG 결제 실패를 성공적으로 처리한다")
    void handlePaymentFailure_success() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
        PgFailCommand command = new PgFailCommand(
                orderId,
                PAYMENT_KEY,
                "INVALID_CARD",
                "유효하지 않은 카드",
                AMOUNT,
                "{}"
        );

        when(pgQueryService.findFailablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgFailService.handlePaymentFailure(command, memberId);

        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.FAILED);
        assertThat(pgPayment.getPaymentKey()).isEqualTo(PAYMENT_KEY);

        verify(pgPaymentFailureRepository).save(any(PgPaymentFailure.class));
        verify(applicationEventPublisher).publishEvent(any(PaymentFailedTxEvent.class));
    }

    @Test
    @DisplayName("PG 결제 실패 시 이벤트가 발행된다")
    void handlePaymentFailure_publishesEvent() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
        PgFailCommand command = new PgFailCommand(
                orderId,
                PAYMENT_KEY,
                "TIMEOUT",
                "타임아웃 발생",
                AMOUNT,
                "{}"
        );

        when(pgQueryService.findFailablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgFailService.handlePaymentFailure(command, memberId);

        ArgumentCaptor<PaymentFailedTxEvent> captor = ArgumentCaptor.forClass(PaymentFailedTxEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().pgPayment().getStatus()).isEqualTo(PgPaymentStatus.FAILED);
    }

    @Test
    @DisplayName("시스템 에러로 인한 결제 실패를 성공적으로 처리한다")
    void markFailedPaymentBySystem_success() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
        String errorMessage = "Database connection failed";

        when(pgQueryService.findFailablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgFailService.markFailedPaymentBySystem(errorMessage, PAYMENT_KEY, orderId, memberId);

        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.FAILED);
        assertThat(pgPayment.getPaymentKey()).isEqualTo(PAYMENT_KEY);

        verify(pgPaymentFailureRepository).save(any(PgPaymentFailure.class));
        verify(applicationEventPublisher).publishEvent(any(PaymentFailedTxEvent.class));
    }

    @Test
    @DisplayName("시스템 에러 처리 시 이벤트가 발행된다")
    void markFailedPaymentBySystem_publishesEvent() {
        PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
        String errorMessage = "Internal server error";

        when(pgQueryService.findFailablePayment(orderId, memberId)).thenReturn(pgPayment);

        pgFailService.markFailedPaymentBySystem(errorMessage, PAYMENT_KEY, orderId, memberId);

        verify(pgQueryService).findFailablePayment(orderId, memberId);
        verify(applicationEventPublisher).publishEvent(any(PaymentFailedTxEvent.class));
    }
}
