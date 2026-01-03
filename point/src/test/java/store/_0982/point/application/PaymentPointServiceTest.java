package store._0982.point.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PaymentPointCommand;
import store._0982.point.application.dto.PaymentPointCreateInfo;
import store._0982.point.application.dto.PointChargeConfirmCommand;
import store._0982.point.application.dto.PointChargeFailCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;
import store._0982.point.domain.event.PointRechargedEvent;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.repository.PaymentPointFailureRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPointServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PaymentPointRepository paymentPointRepository;

    @Mock
    private MemberPointRepository memberPointRepository;

    @Mock
    private PaymentPointFailureRepository paymentPointFailureRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PaymentPointService paymentPointService;

    @Nested
    @DisplayName("주문 생성")
    class CreatePaymentPoint {

        @Test
        @DisplayName("포인트 충전 주문을 생성한다")
        void createPaymentPoint_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            PaymentPointCommand command = new PaymentPointCommand(orderId, 10000);

            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
            when(paymentPointRepository.saveAndFlush(any(PaymentPoint.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            PaymentPointCreateInfo result = paymentPointService.createPaymentPoint(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.amount()).isEqualTo(10000);
            verify(paymentPointRepository).saveAndFlush(any(PaymentPoint.class));
        }

        @Test
        @DisplayName("이미 존재하는 주문번호로 생성 요청 시 기존 정보를 반환한다")
        void createPaymentPoint_returnExisting() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            PaymentPointCommand command = new PaymentPointCommand(orderId, 10000);

            PaymentPoint existingPayment = PaymentPoint.create(memberId, orderId, 10000);
            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPayment));

            // when
            PaymentPointCreateInfo result = paymentPointService.createPaymentPoint(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            verify(paymentPointRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class ConfirmPayment {

        @Test
        @DisplayName("결제 승인을 완료하고 포인트를 충전한다")
        void confirmPayment_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);

            PointChargeConfirmCommand command = new PointChargeConfirmCommand(
                    orderId,
                    10000,
                    "test_payment_key"
            );

            TossPaymentResponse tossResponse = new TossPaymentResponse(
                    "test_payment_key",
                    orderId,
                    10000,
                    "CARD",
                    "DONE",
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    null
            );

            MemberPoint memberPoint = new MemberPoint(memberId);

            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));
            when(tossPaymentService.confirmPayment(any(), any())).thenReturn(tossResponse);
            when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
            doNothing().when(applicationEventPublisher).publishEvent(any(PointRechargedEvent.class));

            // when
            paymentPointService.confirmPayment(command);

            // then
            assertThat(memberPoint.getPointBalance()).isEqualTo(10000);
            verify(applicationEventPublisher).publishEvent(any(PointRechargedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 승인 요청 시 예외가 발생한다")
        void confirmPayment_fail_whenPaymentNotFound() {
            // given
            UUID orderId = UUID.randomUUID();
            PointChargeConfirmCommand command = new PointChargeConfirmCommand(
                    orderId,
                    10000,
                    "test_payment_key"
            );

            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentPointService.confirmPayment(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 승인된 주문으로 재승인 시 예외가 발생한다")
        void confirmPayment_fail_whenAlreadyCompleted() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
            paymentPoint.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

            PointChargeConfirmCommand command = new PointChargeConfirmCommand(
                    orderId,
                    10000,
                    "test_payment_key"
            );

            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));

            // when & then
            assertThatThrownBy(() -> paymentPointService.confirmPayment(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class HandlePaymentFailure {

        @Test
        @DisplayName("결제 실패 정보를 저장한다")
        void handlePaymentFailure_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID orderId = UUID.randomUUID();
            PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);

            PointChargeFailCommand command = new PointChargeFailCommand(
                    orderId,
                    "test_payment_key",
                    "PAYMENT_FAILED",
                    "카드 승인 실패",
                    10000L,
                    "{}"
            );

            PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);

            when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));
            when(paymentPointFailureRepository.save(any(PaymentPointFailure.class))).thenReturn(failure);

            // when
            paymentPointService.handlePaymentFailure(command);

            // then
            verify(paymentPointFailureRepository).save(any(PaymentPointFailure.class));
        }
    }
}
