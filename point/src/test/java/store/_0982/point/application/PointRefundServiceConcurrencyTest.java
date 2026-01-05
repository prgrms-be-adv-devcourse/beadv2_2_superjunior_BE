package store._0982.point.application;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.infrastructure.MemberPointJpaRepository;
import store._0982.point.infrastructure.PaymentPointJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PointRefundServiceConcurrencyTest extends BaseConcurrencyTest {

    private static final int INITIAL_BALANCE = 50_000;
    private static final int PAYMENT_AMOUNT = 10_000;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PointRefundService pointRefundService;

    @Autowired
    private PaymentPointJpaRepository paymentPointRepository;

    @Autowired
    private MemberPointJpaRepository memberPointRepository;

    @MockitoBean
    private TossPaymentService tossPaymentService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        paymentPointRepository.deleteAll();
        memberPointRepository.deleteAll();

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(INITIAL_BALANCE + PAYMENT_AMOUNT);
        memberPointRepository.save(memberPoint);

        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, PAYMENT_AMOUNT);
        paymentPoint.markConfirmed("카드", OffsetDateTime.now(), "test-payment-key");
        paymentPointRepository.save(paymentPoint);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 환불 요청을 중복 처리하지 않는다")
    void concurrent_refund_idempotent() throws InterruptedException {
        // given
        String cancelReason = "단순 변심";
        PointRefundCommand command = new PointRefundCommand(orderId, cancelReason);
        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                PAYMENT_AMOUNT,
                cancelReason,
                OffsetDateTime.now()
        );
        TossPaymentResponse response = new TossPaymentResponse(
                "test-payment-key",
                orderId,
                PAYMENT_AMOUNT,
                "카드",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of(cancelInfo)
        );

        when(tossPaymentService.cancelPayment(any(PaymentPoint.class), any(PointRefundCommand.class)))
                .thenReturn(response);

        // when
        runSynchronizedTask(() -> pointRefundService.refundPaymentPoint(memberId, command));

        // then
        validate();
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 환불 요청을 중복 처리하지 않는다")
    void concurrent_refund_duplicate() throws InterruptedException {
        // given
        List<PointRefundCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointRefundCommand.class)
                .instantiate(Instantiator.constructor())
                .set("orderId", orderId)
                .setNotNull("cancelReason")
                .sampleList(getDefaultThreadCount());

        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                PAYMENT_AMOUNT,
                "단순 변심",
                OffsetDateTime.now()
        );
        TossPaymentResponse response = new TossPaymentResponse(
                "test-payment-key",
                orderId,
                PAYMENT_AMOUNT,
                "카드",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of(cancelInfo)
        );

        when(tossPaymentService.cancelPayment(any(PaymentPoint.class), any(PointRefundCommand.class)))
                .thenReturn(response);

        // when
        runSynchronizedTasks(commands, command -> pointRefundService.refundPaymentPoint(memberId, command));

        // then
        validate();
    }

    private void validate() {
        PaymentPoint paymentPoint = paymentPointRepository.findByPgOrderId(orderId).orElseThrow();
        assertThat(paymentPoint.getStatus()).isEqualTo(PaymentPointStatus.REFUNDED);
        assertThat(paymentPoint.getRefundedAt()).isNotNull();
        assertThat(paymentPoint.getRefundMessage()).isNotNull();

        MemberPoint memberPoint = memberPointRepository.findById(memberId).orElseThrow();
        assertThat(memberPoint.getPointBalance()).isEqualTo(INITIAL_BALANCE);

        verify(tossPaymentService, times(1)).cancelPayment(any(PaymentPoint.class), any(PointRefundCommand.class));
    }
}
