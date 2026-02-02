package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.pg.PgConfirmFacade;
import store._0982.point.application.pg.PgConfirmService;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.NegligibleWebhookErrorType;
import store._0982.point.exception.NegligibleWebhookException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TossWebhookServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PgConfirmFacade pgConfirmFacade;

    @Mock
    private PgConfirmService pgConfirmService;

    @Mock
    private PgCancelService pgCancelService;

    @Mock
    private PgFailService pgFailService;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @InjectMocks
    private TossWebhookService tossWebhookService;

    private UUID orderId;
    private UUID memberId;
    private String paymentKey;
    private long amount;
    private PgPayment pgPayment;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        paymentKey = "test_payment_key";
        amount = 10000L;
        pgPayment = PgPayment.create(memberId, orderId, amount, "테스트 공구");
    }

    @Nested
    @DisplayName("결제 실패 (ABORTED/EXPIRED) 처리")
    class HandleFailed {

        @Test
        @DisplayName("PENDING 상태의 결제를 FAILED로 변경한다")
        void handleFailed_pending_to_failed() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createFailedPaymentInfo();
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgFailService).handlePaymentFailure(any(PgFailCommand.class), eq(memberId));
        }

        @Test
        @DisplayName("FAILED 상태의 결제는 무시한다")
        void handleFailed_already_failed_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createFailedPaymentInfo();
            pgPayment.markFailed(paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgFailService, never()).handlePaymentFailure(any(), any());
        }

        @Test
        @DisplayName("REFUNDED 상태의 결제는 무시한다")
        void handleFailed_refunded_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createFailedPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgFailService, never()).handlePaymentFailure(any(), any());
        }

        @Test
        @DisplayName("COMPLETED 상태의 결제를 FAILED로 변경하려 하면 예외가 발생한다")
        void handleFailed_completed_throws_exception() {
            // given
            TossPaymentInfo tossPaymentInfo = createFailedPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when & then
            assertThatThrownBy(() -> tossWebhookService.processWebhookPayment(tossPaymentInfo))
                    .isInstanceOf(NegligibleWebhookException.class)
                    .hasMessageContaining(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH.getMessage());

            verify(pgFailService, never()).handlePaymentFailure(any(), any());
        }

        private TossPaymentInfo createFailedPaymentInfo() {
            return TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.ABORTED)
                    .requestedAt(OffsetDateTime.now())
                    .failure(TossPaymentInfo.FailureInfo.builder()
                            .code("PAYMENT_FAILED")
                            .message("결제 실패")
                            .build())
                    .build();
        }
    }

    @Nested
    @DisplayName("전액 취소 (CANCELED) 처리")
    class HandleCanceled {

        @Test
        @DisplayName("PENDING 상태의 결제를 REFUNDED로 변경한다")
        void handleCanceled_pending_to_refunded() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCanceledPaymentInfo();
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("COMPLETED 상태의 결제를 REFUNDED로 변경한다")
        void handleCanceled_completed_to_refunded() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService).markRefundedPayment(tossPaymentInfo, orderId, memberId);
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태의 결제를 REFUNDED로 변경한다")
        void handleCanceled_partially_to_refunded() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.applyRefund(1L, OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService).markRefundedPayment(tossPaymentInfo, orderId, memberId);
        }

        @Test
        @DisplayName("REFUNDED 상태의 결제는 무시한다")
        void handleCanceled_already_refunded_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("다중 cancels가 있을 때 첫 번째 canceledAt을 사용한다")
        void handleCanceled_multiple_cancels_uses_first() throws JsonProcessingException {
            // given
            TossPaymentInfo.CancelInfo cancel1 = TossPaymentInfo.CancelInfo.builder()
                    .cancelAmount(5000L)
                    .cancelReason("부분 취소 1")
                    .canceledAt(OffsetDateTime.now())
                    .transactionKey("key1")
                    .build();

            TossPaymentInfo.CancelInfo cancel2 = TossPaymentInfo.CancelInfo.builder()
                    .cancelAmount(5000L)
                    .cancelReason("부분 취소 2")
                    .canceledAt(OffsetDateTime.now().plusMinutes(1))
                    .transactionKey("key2")
                    .build();

            TossPaymentInfo tossPaymentInfo = TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.CANCELED)
                    .requestedAt(OffsetDateTime.now())
                    .approvedAt(OffsetDateTime.now())
                    .cancels(List.of(cancel1, cancel2))
                    .build();

            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
        }

        private TossPaymentInfo createCanceledPaymentInfo() {
            TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                    .cancelAmount(amount)
                    .cancelReason("고객 요청")
                    .canceledAt(OffsetDateTime.now())
                    .transactionKey("test_transaction_key")
                    .build();

            return TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.CANCELED)
                    .requestedAt(OffsetDateTime.now())
                    .approvedAt(OffsetDateTime.now())
                    .cancels(List.of(cancelInfo))
                    .build();
        }
    }

    @Nested
    @DisplayName("부분 취소 (PARTIAL_CANCELED) 처리")
    class HandlePartiallyCanceled {

        @Test
        @DisplayName("COMPLETED 상태의 결제를 PARTIALLY_REFUNDED로 변경한다")
        void handlePartiallyCanceled_completed_to_partially_refunded() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createPartiallyCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService).markRefundedPayment(tossPaymentInfo, orderId, memberId);
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태의 결제에 추가 환불을 처리한다")
        void handlePartiallyCanceled_add_more_refund() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createPartiallyCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.applyRefund(1L, OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgCancelService).markRefundedPayment(tossPaymentInfo, orderId, memberId);
        }

        @Test
        @DisplayName("PENDING 상태의 결제를 부분 취소하려 하면 예외가 발생한다")
        void handlePartiallyCanceled_pending_throws_exception() {
            // given
            TossPaymentInfo tossPaymentInfo = createPartiallyCanceledPaymentInfo();
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when & then
            assertThatThrownBy(() -> tossWebhookService.processWebhookPayment(tossPaymentInfo))
                    .isInstanceOf(NegligibleWebhookException.class)
                    .hasMessageContaining(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH.getMessage());

            verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("FAILED 상태의 결제를 부분 취소하려 하면 예외가 발생한다")
        void handlePartiallyCanceled_failed_throws_exception() {
            // given
            TossPaymentInfo tossPaymentInfo = createPartiallyCanceledPaymentInfo();
            pgPayment.markFailed(paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when & then
            assertThatThrownBy(() -> tossWebhookService.processWebhookPayment(tossPaymentInfo))
                    .isInstanceOf(NegligibleWebhookException.class)
                    .hasMessageContaining(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH.getMessage());

            verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("REFUNDED 상태의 결제를 부분 취소하려 하면 예외가 발생한다")
        void handlePartiallyCanceled_refunded_throws_exception() {
            // given
            TossPaymentInfo tossPaymentInfo = createPartiallyCanceledPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when & then
            assertThatThrownBy(() -> tossWebhookService.processWebhookPayment(tossPaymentInfo))
                    .isInstanceOf(NegligibleWebhookException.class)
                    .hasMessageContaining(NegligibleWebhookErrorType.PAYMENT_STATUS_MISMATCH.getMessage());

            verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        }

        private TossPaymentInfo createPartiallyCanceledPaymentInfo() {
            TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                    .cancelAmount(5000L)
                    .cancelReason("부분 취소")
                    .canceledAt(OffsetDateTime.now())
                    .transactionKey("test_transaction_key")
                    .build();

            return TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.PARTIAL_CANCELED)
                    .requestedAt(OffsetDateTime.now())
                    .approvedAt(OffsetDateTime.now())
                    .cancels(List.of(cancelInfo))
                    .build();
        }
    }

    @Nested
    @DisplayName("결제 완료 (DONE) 처리")
    class HandleCompleted {

        @Test
        @DisplayName("PENDING 상태의 결제를 COMPLETED로 변경한다")
        void handleCompleted_pending_to_completed() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCompletedPaymentInfo();
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmService).markConfirmedPayment(tossPaymentInfo, orderId, memberId);
        }

        @Test
        @DisplayName("COMPLETED 상태의 결제는 무시한다")
        void handleCompleted_already_completed_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCompletedPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmService, never()).markConfirmedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("REFUNDED 상태의 결제는 무시한다")
        void handleCompleted_refunded_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCompletedPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmService, never()).markConfirmedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태의 결제는 무시한다")
        void handleCompleted_partially_refunded_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCompletedPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPayment.applyRefund(1L, OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmService, never()).markConfirmedPayment(any(), any(), any());
        }

        @Test
        @DisplayName("FAILED 상태의 결제는 무시한다")
        void handleCompleted_failed_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createCompletedPaymentInfo();
            pgPayment.markFailed(paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmService, never()).markConfirmedPayment(any(), any(), any());
        }

        private TossPaymentInfo createCompletedPaymentInfo() {
            return TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.DONE)
                    .requestedAt(OffsetDateTime.now())
                    .approvedAt(OffsetDateTime.now())
                    .build();
        }
    }

    @Nested
    @DisplayName("결제 진행중 (IN_PROGRESS) 처리")
    class HandleInProgress {

        @Test
        @DisplayName("PENDING 상태의 결제를 승인한다")
        void handleInProgress_pending_confirm() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createInProgressPaymentInfo();
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmFacade).confirmPayment(any(PgConfirmCommand.class), eq(memberId));
        }

        @Test
        @DisplayName("COMPLETED 상태의 결제는 무시한다")
        void handleInProgress_completed_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createInProgressPaymentInfo();
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmFacade, never()).confirmPayment(any(), any());
        }

        @Test
        @DisplayName("종료 상태의 결제는 무시한다")
        void handleInProgress_terminal_states_ignored() throws JsonProcessingException {
            // given
            TossPaymentInfo tossPaymentInfo = createInProgressPaymentInfo();
            pgPayment.markFailed(paymentKey);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            // when
            tossWebhookService.processWebhookPayment(tossPaymentInfo);

            // then
            verify(pgPaymentRepository).findByOrderId(orderId);
            verify(pgConfirmFacade, never()).confirmPayment(any(), any());
        }

        private TossPaymentInfo createInProgressPaymentInfo() {
            return TossPaymentInfo.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .method("카드")
                    .status(TossPaymentInfo.Status.IN_PROGRESS)
                    .requestedAt(OffsetDateTime.now())
                    .build();
        }
    }

    @Test
    @DisplayName("READY/WAITING_FOR_DEPOSIT 상태는 아무 처리도 하지 않는다")
    void handle_ready_or_waiting_ignored() throws JsonProcessingException {
        // given
        TossPaymentInfo readyPaymentInfo = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("가상계좌")
                .status(TossPaymentInfo.Status.READY)
                .requestedAt(OffsetDateTime.now())
                .build();

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when
        tossWebhookService.processWebhookPayment(readyPaymentInfo);

        // then
        verify(pgPaymentRepository).findByOrderId(orderId);
        verify(pgConfirmFacade, never()).confirmPayment(any(), any());
        verify(pgConfirmService, never()).markConfirmedPayment(any(), any(), any());
        verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        verify(pgFailService, never()).handlePaymentFailure(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 orderId로 웹훅이 오면 NegligibleWebhookException을 발생시킨다")
    void processWebhookPayment_payment_not_found() {
        // given
        TossPaymentInfo tossPaymentInfo = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tossWebhookService.processWebhookPayment(tossPaymentInfo))
                .isInstanceOf(NegligibleWebhookException.class)
                .hasMessageContaining(NegligibleWebhookErrorType.PAYMENT_NOT_FOUND.getMessage());

        verify(pgPaymentRepository).findByOrderId(orderId);
        verify(pgConfirmFacade, never()).confirmPayment(any(), any());
        verify(pgCancelService, never()).markRefundedPayment(any(), any(), any());
        verify(pgFailService, never()).handlePaymentFailure(any(), any());
    }
}
