package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgConfirmServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PgTxManager pgTxManager;

    @Mock
    private CommerceServiceClient commerceServiceClient;

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

    @Test
    @DisplayName("결제 승인을 완료하고 포인트를 충전한다")
    void confirmPayment_success() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);

        PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

        TossPaymentInfo tossResponse = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(10000)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        OrderInfo orderInfo = new OrderInfo(
                orderId,
                10000,
                OrderInfo.Status.PENDING,
                memberId,
                1
        );

        // Mocks - PgTransactionManager 사용
        when(pgTxManager.findCompletablePayment(paymentKey, memberId))
                .thenReturn(pgPayment);
        when(commerceServiceClient.getOrder(orderId, memberId))
                .thenReturn(orderInfo);
        when(tossPaymentService.confirmPayment(any(), any()))
                .thenReturn(tossResponse);
        doNothing().when(pgTxManager)
                .markConfirmedPayment(any(), eq(paymentKey), eq(memberId));

        // when
        pgConfirmService.confirmPayment(command, memberId);

        // then
        verify(pgTxManager).markConfirmedPayment(
                any(TossPaymentInfo.class), eq(paymentKey), eq(memberId)
        );
        verify(commerceServiceClient).getOrder(orderId, memberId);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 승인 요청 시 예외가 발생한다")
    void confirmPayment_fail_whenPaymentNotFound() {
        // given
        PgConfirmCommand command = new PgConfirmCommand(orderId, 10000, paymentKey);

        when(pgTxManager.findCompletablePayment(paymentKey, memberId))
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

        when(pgTxManager.findCompletablePayment(paymentKey, memberId))
                .thenThrow(new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT));

        // when & then
        assertThatThrownBy(() -> pgConfirmService.confirmPayment(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
    }
}
