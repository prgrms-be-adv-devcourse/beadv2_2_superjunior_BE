package store._0982.point.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.TossPaymentClient;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTest {

    @Mock
    private TossPaymentClient tossPaymentClient;

    @InjectMocks
    private TossPaymentService tossPaymentService;

    @Test
    @DisplayName("결제 승인이 성공한다")
    void confirmPayment_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        PaymentConfirmCommand command = new PaymentConfirmCommand(
                orderId,
                10000,
                "test_payment_key"
        );

        TossPaymentResponse response = new TossPaymentResponse(
                "test_payment_key",
                orderId,
                10000,
                "CARD",
                "DONE",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null
        );

        when(tossPaymentClient.confirm(any(TossPaymentConfirmRequest.class))).thenReturn(response);

        // when
        TossPaymentResponse result = tossPaymentService.confirmPayment(payment, command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        verify(tossPaymentClient).confirm(any(TossPaymentConfirmRequest.class));
    }

    @Test
    @DisplayName("orderId가 일치하지 않으면 예외가 발생한다")
    void confirmPayment_fail_whenOrderIdMismatch() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        PaymentConfirmCommand command = new PaymentConfirmCommand(
                orderId,
                10000,
                "test_payment_key"
        );

        UUID differentOrderId = UUID.randomUUID();
        TossPaymentResponse response = new TossPaymentResponse(
                "test_payment_key",
                differentOrderId,  // 다른 orderId
                10000,
                "CARD",
                "DONE",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null
        );

        when(tossPaymentClient.confirm(any(TossPaymentConfirmRequest.class))).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(payment, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ORDER_ID_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("외부 API 타임아웃 시 예외가 발생한다")
    void confirmPayment_fail_whenTimeout() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        PaymentConfirmCommand command = new PaymentConfirmCommand(
                orderId,
                10000,
                "test_payment_key"
        );

        when(tossPaymentClient.confirm(any(TossPaymentConfirmRequest.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> tossPaymentService.confirmPayment(payment, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_API_TIMEOUT.getMessage());
    }

    @Test
    @DisplayName("결제 취소가 성공한다")
    void cancelPayment_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);
        payment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        PointRefundCommand command = new PointRefundCommand(orderId, "환불 요청");

        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                10000,
                "환불 요청",
                OffsetDateTime.now()
        );

        TossPaymentResponse response = new TossPaymentResponse(
                "test_payment_key",
                orderId,
                10000,
                "CARD",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                java.util.List.of(cancelInfo)
        );

        when(tossPaymentClient.cancel(any())).thenReturn(response);

        // when
        TossPaymentResponse result = tossPaymentService.cancelPayment(payment, command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        verify(tossPaymentClient).cancel(any());
    }

    @Test
    @DisplayName("외부 API 에러 발생 시 예외가 발생한다")
    void cancelPayment_fail_whenApiError() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        PointRefundCommand command = new PointRefundCommand(orderId, "환불 요청");

        when(tossPaymentClient.cancel(any()))
                .thenThrow(new RuntimeException("API Error"));

        // when & then
        assertThatThrownBy(() -> tossPaymentService.cancelPayment(payment, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_API_ERROR.getMessage());
    }
}
