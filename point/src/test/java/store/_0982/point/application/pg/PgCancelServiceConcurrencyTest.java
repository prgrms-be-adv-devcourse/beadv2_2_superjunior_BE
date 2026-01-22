package store._0982.point.application.pg;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.infrastructure.pg.PgPaymentCancelJpaRepository;
import store._0982.point.infrastructure.pg.PgPaymentJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PgCancelServiceConcurrencyTest extends BaseConcurrencyTest {

    private static final long PAYMENT_AMOUNT = 10_000;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PgCancelService pgCancelService;

    @Autowired
    private PgPaymentJpaRepository paymentPointRepository;

    @Autowired
    private PgPaymentCancelJpaRepository paymentCancelRepository;

    @MockitoBean
    private TossPaymentService tossPaymentService;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID memberId;
    private UUID orderId;
    private TossPaymentInfo tossPaymentInfo;

    @BeforeEach
    void setUp() {
        paymentCancelRepository.deleteAll();
        paymentPointRepository.deleteAll();

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(PAYMENT_AMOUNT)
                .cancelReason("단순 변심")
                .canceledAt(OffsetDateTime.now())
                .transactionKey(UUID.randomUUID().toString())
                .build();

        tossPaymentInfo = TossPaymentInfo.builder()
                .paymentKey("test-payment-key")
                .orderId(orderId)
                .amount(PAYMENT_AMOUNT)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();

        PgPayment pgPayment = PgPayment.create(memberId, orderId, PAYMENT_AMOUNT);
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), "test-payment-key");
        paymentPointRepository.save(pgPayment);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 환불 요청을 중복 처리하지 않는다")
    void concurrent_refund_idempotent() throws InterruptedException {
        // given
        String cancelReason = "단순 변심";
        PgCancelCommand command = new PgCancelCommand(orderId, cancelReason, PAYMENT_AMOUNT);

        when(tossPaymentService.cancelPayment(any(PgPayment.class), any(PgCancelCommand.class)))
                .thenReturn(tossPaymentInfo);

        // when
        runSynchronizedTask(() -> pgCancelService.refundPayment(memberId, command));

        // then
        validateOwner();
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 환불 요청을 중복 처리하지 않는다")
    void concurrent_refund_duplicate() throws InterruptedException {
        // given
        List<PgCancelCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PgCancelCommand.class)
                .instantiate(Instantiator.constructor())
                .set("orderId", orderId)
                .setNotNull("cancelReason")
                .sampleList(getDefaultThreadCount());

        when(tossPaymentService.cancelPayment(any(PgPayment.class), any(PgCancelCommand.class)))
                .thenReturn(tossPaymentInfo);

        // when
        runSynchronizedTasks(commands, command -> pgCancelService.refundPayment(memberId, command));

        // then
        validateOwner();
    }

    private void validateOwner() {
        PgPayment pgPayment = paymentPointRepository.findByOrderId(orderId).orElseThrow();
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);
        assertThat(pgPayment.getRefundedAt()).isNotNull();
    }
}
