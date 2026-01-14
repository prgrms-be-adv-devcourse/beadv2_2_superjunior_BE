package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PgConfirmCommand;
import store._0982.point.application.dto.PgCreateCommand;
import store._0982.point.application.dto.PgCreateInfo;
import store._0982.point.application.pg.PgConfirmService;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.application.pg.PgTransactionManager;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.PgPayment;
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
class PgPaymentServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PgTransactionManager pgTransactionManager;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private PgPaymentService pgPaymentService;

    @InjectMocks
    private PgConfirmService pgConfirmService;

    private UUID memberId;
    private UUID orderId;
    private String paymentKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
    }

    @Nested
    @DisplayName("주문 생성")
    class CreatePgPayment {

        @Test
        @DisplayName("포인트 충전 주문을 생성한다")
        void createPaymentPoint_success() {
            // given
            PgCreateCommand command = new PgCreateCommand(orderId, 10000);

            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
            when(pgPaymentRepository.saveAndFlush(any(PgPayment.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            PgCreateInfo result = pgPaymentService.createPayment(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.amount()).isEqualTo(10000);
            verify(pgPaymentRepository).saveAndFlush(any(PgPayment.class));
        }

        @Test
        @DisplayName("이미 존재하는 주문번호로 생성 요청 시 기존 정보를 반환한다")
        void createPaymentPoint_returnExisting() {
            // given
            PgCreateCommand command = new PgCreateCommand(orderId, 10000);

            PgPayment existingPgPayment = PgPayment.create(memberId, orderId, 10000);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingPgPayment));

            // when
            PgCreateInfo result = pgPaymentService.createPayment(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            verify(pgPaymentRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class ConfirmPgPayment {

        @Test
        @DisplayName("결제 승인을 완료하고 포인트를 충전한다")
        void confirmPayment_success() {
            // given
            PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);

            PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

            TossPaymentResponse tossResponse = new TossPaymentResponse(
                    paymentKey,
                    orderId,
                    10000,
                    "CARD",
                    "DONE",
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    null
            );

            OrderInfo orderInfo = new OrderInfo(
                    orderId,
                    10000,
                    OrderInfo.Status.PENDING,
                    memberId,
                    1
            );

            // Mocks - PgTransactionManager 사용
            when(pgTransactionManager.findCompletablePayment(paymentKey, memberId))
                    .thenReturn(pgPayment);
            when(orderServiceClient.getOrder(orderId, memberId))
                    .thenReturn(orderInfo);
            when(tossPaymentService.confirmPayment(any(), any()))
                    .thenReturn(tossResponse);
            doNothing().when(pgTransactionManager)
                    .markConfirmedPayment(any(), eq(paymentKey), eq(memberId));

            // when
            pgConfirmService.confirmPayment(command, memberId);

            // then
            verify(pgTransactionManager).markConfirmedPayment(
                    any(TossPaymentResponse.class), eq(paymentKey), eq(memberId)
            );
            verify(orderServiceClient).getOrder(orderId, memberId);
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 승인 요청 시 예외가 발생한다")
        void confirmPayment_fail_whenPaymentNotFound() {
            // given
            PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

            when(pgTransactionManager.findCompletablePayment(paymentKey, memberId))
                    .thenThrow(new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> pgConfirmService.confirmPayment(command, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 승인된 주문으로 재승인 시 예외가 발생한다")
        void confirmPayment_fail_whenAlreadyCompleted() {
            // given
            PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

            when(pgTransactionManager.findCompletablePayment(paymentKey, memberId))
                    .thenThrow(new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT));

            // when & then
            assertThatThrownBy(() -> pgConfirmService.confirmPayment(command, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }
    }
}
