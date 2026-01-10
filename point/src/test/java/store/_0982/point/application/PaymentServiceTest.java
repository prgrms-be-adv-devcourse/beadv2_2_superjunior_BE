package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PaymentCreateCommand;
import store._0982.point.application.dto.PaymentCreateInfo;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.event.PaymentConfirmedEvent;
import store._0982.point.domain.repository.PointRepository;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreatePayment {

        @Test
        @DisplayName("포인트 충전 주문을 생성한다")
        void createPaymentPoint_success() {
            // given
            PaymentCreateCommand command = new PaymentCreateCommand(orderId, 10000);

            when(paymentRepository.findByPgOrderId(orderId)).thenReturn(Optional.empty());
            when(paymentRepository.saveAndFlush(any(Payment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            PaymentCreateInfo result = paymentService.createPaymentPoint(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.amount()).isEqualTo(10000);
            verify(paymentRepository).saveAndFlush(any(Payment.class));
        }

        @Test
        @DisplayName("이미 존재하는 주문번호로 생성 요청 시 기존 정보를 반환한다")
        void createPaymentPoint_returnExisting() {
            // given
            PaymentCreateCommand command = new PaymentCreateCommand(orderId, 10000);

            Payment existingPayment = Payment.create(memberId, orderId, 10000);
            when(paymentRepository.findByPgOrderId(orderId)).thenReturn(Optional.of(existingPayment));

            // when
            PaymentCreateInfo result = paymentService.createPaymentPoint(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            verify(paymentRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class ConfirmPayment {

        @Test
        @DisplayName("결제 승인을 완료하고 포인트를 충전한다")
        void confirmPayment_success() {
            // given
            Payment payment = Payment.create(memberId, orderId, 10000);

            PaymentConfirmCommand command = new PaymentConfirmCommand(
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

            Point point = new Point(memberId);

            when(paymentRepository.findByPgOrderId(orderId)).thenReturn(Optional.of(payment));
            when(tossPaymentService.confirmPayment(any(), any())).thenReturn(tossResponse);
            when(pointRepository.findById(memberId)).thenReturn(Optional.of(point));
            doNothing().when(applicationEventPublisher).publishEvent(any(PaymentConfirmedEvent.class));

            // when
            paymentService.confirmPayment(command, memberId);

            // then
            assertThat(point.getTotalBalance()).isEqualTo(10000);
            verify(applicationEventPublisher).publishEvent(any(PaymentConfirmedEvent.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 승인 요청 시 예외가 발생한다")
        void confirmPayment_fail_whenPaymentNotFound() {
            // given
            PaymentConfirmCommand command = new PaymentConfirmCommand(
                    orderId,
                    10000,
                    "test_payment_key"
            );

            when(paymentRepository.findByPgOrderId(orderId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(command, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 승인된 주문으로 재승인 시 예외가 발생한다")
        void confirmPayment_fail_whenAlreadyCompleted() {
            // given
            Payment payment = Payment.create(memberId, orderId, 10000);
            payment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

            PaymentConfirmCommand command = new PaymentConfirmCommand(
                    orderId,
                    10000,
                    "test_payment_key"
            );

            when(paymentRepository.findByPgOrderId(orderId)).thenReturn(Optional.of(payment));

            // when & then
            assertThatThrownBy(() -> paymentService.confirmPayment(command, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }
    }
}
