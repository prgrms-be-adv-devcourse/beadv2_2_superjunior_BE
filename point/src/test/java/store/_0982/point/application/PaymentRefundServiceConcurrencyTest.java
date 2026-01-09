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
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.entity.Payment;
import store._0982.point.infrastructure.PointJpaRepository;
import store._0982.point.infrastructure.PaymentJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentRefundServiceConcurrencyTest extends BaseConcurrencyTest {

    private static final int INITIAL_BALANCE = 50_000;
    private static final int PAYMENT_AMOUNT = 10_000;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PaymentRefundService paymentRefundService;

    @Autowired
    private PaymentJpaRepository paymentPointRepository;

    @Autowired
    private PointJpaRepository memberPointRepository;

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

        Point point = new Point(memberId);
        point.charge(INITIAL_BALANCE + PAYMENT_AMOUNT);
        memberPointRepository.save(point);

        Payment payment = Payment.create(memberId, orderId, PAYMENT_AMOUNT);
        payment.markConfirmed("카드", OffsetDateTime.now(), "test-payment-key");
        paymentPointRepository.save(payment);
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

        when(tossPaymentService.cancelPayment(any(Payment.class), any(PointRefundCommand.class)))
                .thenReturn(response);

        // when
        runSynchronizedTask(() -> paymentRefundService.refundPaymentPoint(memberId, command));

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

        when(tossPaymentService.cancelPayment(any(Payment.class), any(PointRefundCommand.class)))
                .thenReturn(response);

        // when
        runSynchronizedTasks(commands, command -> paymentRefundService.refundPaymentPoint(memberId, command));

        // then
        validate();
    }

    private void validate() {
        Payment payment = paymentPointRepository.findByPgOrderId(orderId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundedAt()).isNotNull();
        assertThat(payment.getRefundMessage()).isNotNull();

        Point point = memberPointRepository.findById(memberId).orElseThrow();
        assertThat(point.getTotalBalance()).isEqualTo(INITIAL_BALANCE);

        verify(tossPaymentService, times(1)).cancelPayment(any(Payment.class), any(PointRefundCommand.class));
    }
}
