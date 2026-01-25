package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.event.PaymentCanceledTxEvent;
import store._0982.point.domain.repository.PgPaymentCancelRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgCancelServiceTest {

    private static final long REFUND_AMOUNT = 10000;
    private static final String PAYMENT_KEY = "test_payment_key";

    @Mock
    private PgQueryService pgQueryService;

    @Mock
    private PgPaymentCancelRepository pgPaymentCancelRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PgCancelService pgCancelService;

    private UUID memberId;
    private UUID orderId;
    private PgPayment pgPayment;
    private TossPaymentInfo tossPaymentInfo;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT, "테스트 공구");
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);

        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(REFUND_AMOUNT)
                .cancelReason("단순 변심")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        tossPaymentInfo = TossPaymentInfo.builder()
                .paymentKey(PAYMENT_KEY)
                .orderId(orderId)
                .amount(REFUND_AMOUNT)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();
    }

    @Test
    @DisplayName("환불 마킹을 성공적으로 처리한다")
    void markRefundedPayment_success() {
        when(pgQueryService.findRefundablePayment(orderId, memberId)).thenReturn(pgPayment);
        when(pgPaymentCancelRepository.findExistingTransactionKeys(anyList())).thenReturn(Set.of());

        pgCancelService.markRefundedPayment(tossPaymentInfo, orderId, memberId);

        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);
        verify(pgPaymentCancelRepository).saveAllAndFlush(anyList());
        verify(applicationEventPublisher).publishEvent(any(PaymentCanceledTxEvent.class));
    }

    @Test
    @DisplayName("부분 환불 시 PARTIALLY_REFUNDED 상태로 변경된다")
    void markRefundedPayment_partialRefund() {
        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(5000L)
                .cancelReason("부분 취소")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        TossPaymentInfo partialRefundInfo = TossPaymentInfo.builder()
                .paymentKey(PAYMENT_KEY)
                .orderId(orderId)
                .amount(REFUND_AMOUNT)
                .method("카드")
                .status(TossPaymentInfo.Status.PARTIAL_CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();

        when(pgQueryService.findRefundablePayment(orderId, memberId)).thenReturn(pgPayment);
        when(pgPaymentCancelRepository.findExistingTransactionKeys(anyList())).thenReturn(Set.of());

        pgCancelService.markRefundedPayment(partialRefundInfo, orderId, memberId);

        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.PARTIALLY_REFUNDED);
        verify(pgPaymentCancelRepository).saveAllAndFlush(anyList());
    }

    @Test
    @DisplayName("중복 transactionKey는 무시하고 새로운 취소만 처리한다")
    void markRefundedPayment_skipDuplicateTransactionKey() {
        String duplicateKey = "duplicate_transaction_key";
        String newKey = "new_transaction_key";

        TossPaymentInfo.CancelInfo duplicateCancel = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(5000L)
                .cancelReason("중복 취소")
                .canceledAt(OffsetDateTime.now())
                .transactionKey(duplicateKey)
                .build();

        TossPaymentInfo.CancelInfo newCancel = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(5000L)
                .cancelReason("새로운 취소")
                .canceledAt(OffsetDateTime.now())
                .transactionKey(newKey)
                .build();

        TossPaymentInfo multiCancelInfo = TossPaymentInfo.builder()
                .paymentKey(PAYMENT_KEY)
                .orderId(orderId)
                .amount(REFUND_AMOUNT)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(duplicateCancel, newCancel))
                .build();

        when(pgQueryService.findRefundablePayment(orderId, memberId)).thenReturn(pgPayment);
        when(pgPaymentCancelRepository.findExistingTransactionKeys(anyList())).thenReturn(Set.of(duplicateKey));

        pgCancelService.markRefundedPayment(multiCancelInfo, orderId, memberId);

        verify(pgPaymentCancelRepository).saveAllAndFlush(
                argThat(list -> list != null && list.size() == 1)
        );
    }

    @Test
    @DisplayName("환불 마킹 시 이벤트가 발행된다")
    void markRefundedPayment_publishesEvent() {
        when(pgQueryService.findRefundablePayment(orderId, memberId)).thenReturn(pgPayment);
        when(pgPaymentCancelRepository.findExistingTransactionKeys(anyList())).thenReturn(Set.of());

        pgCancelService.markRefundedPayment(tossPaymentInfo, orderId, memberId);

        verify(pgQueryService).findRefundablePayment(orderId, memberId);
        verify(applicationEventPublisher).publishEvent(any(PaymentCanceledTxEvent.class));
    }
}
