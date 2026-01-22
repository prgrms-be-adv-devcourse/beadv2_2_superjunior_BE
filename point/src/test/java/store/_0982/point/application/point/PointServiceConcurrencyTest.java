package store._0982.point.application.point;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class PointServiceConcurrencyTest extends BaseConcurrencyTest {

    private static final long BALANCE = 100_000;
    private static final long AMOUNT = 1_000;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PointDeductService pointDeductService;

    @Autowired
    private PointReturnService pointReturnService;

    @Autowired
    private PointBalanceJpaRepository pointBalanceRepository;

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @MockitoBean
    private CommerceServiceClient commerceServiceClient;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        pointBalanceRepository.deleteAll();
        pointTransactionRepository.deleteAll();

        memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(BALANCE);
        pointBalanceRepository.save(pointBalance);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_use_idempotent() throws InterruptedException {
        // given
        PointDeductCommand command = new PointDeductCommand(UUID.randomUUID(), UUID.randomUUID(), AMOUNT);
        OrderInfo orderInfo = OrderInfo.builder()
                .orderId(command.orderId())
                .price(command.amount())
                .quantity(1)
                .memberId(memberId)
                .status(OrderInfo.Status.PENDING)
                .build();

        when(commerceServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTask(() -> pointDeductService.processDeductionWithBonus(memberId, command));

        // then
        validateDeduction();
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_use_duplicate() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();
        List<PointDeductCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointDeductCommand.class)
                .instantiate(Instantiator.constructor())
                .set("amount", AMOUNT)
                .set("orderId", orderId)
                .setNotNull("idempotencyKey")
                .sampleList(getDefaultThreadCount());

        OrderInfo orderInfo = OrderInfo.builder()
                .orderId(orderId)
                .price(AMOUNT)
                .quantity(1)
                .memberId(memberId)
                .status(OrderInfo.Status.PENDING)
                .build();

        when(commerceServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTasks(commands, command -> pointDeductService.processDeductionWithBonus(memberId, command));

        // then
        validateDeduction();
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_idempotent() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();

        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        PointAmount deduction = pointBalance.use(AMOUNT);

        PointTransaction usedTransaction = PointTransaction.used(memberId, orderId, UUID.randomUUID(), deduction);
        pointTransactionRepository.save(usedTransaction);
        pointBalanceRepository.save(pointBalance);

        PointReturnCommand command = new PointReturnCommand(
                idempotencyKey, orderId, "테스트 환불", AMOUNT);

        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTask(() -> pointReturnService.returnPoints(memberId, command));

        // then
        validateReturn();
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_duplicate() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();

        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        PointAmount deduction = pointBalance.use(AMOUNT);

        PointTransaction usedTransaction = PointTransaction.used(memberId, orderId, UUID.randomUUID(), deduction);
        pointTransactionRepository.save(usedTransaction);
        pointBalanceRepository.save(pointBalance);

        List<PointReturnCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointReturnCommand.class)
                .instantiate(Instantiator.constructor())
                .set("amount", AMOUNT)
                .set("orderId", orderId)
                .setNotNull("idempotencyKey")
                .setNotNull("cancelReason")
                .sampleList(getDefaultThreadCount());

        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTasks(commands, command ->
                pointReturnService.returnPoints(memberId, command));

        // then
        validateReturn();
    }

    private void validateDeduction() {
        assertThat(pointTransactionRepository.count()).isEqualTo(1);
        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(pointBalance.getTotalBalance()).isEqualTo(BALANCE - AMOUNT);
    }

    private void validateReturn() {
        assertThat(pointTransactionRepository.count()).isEqualTo(2); // USED + RETURNED (1개만)
        PointBalance finalBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(finalBalance.getTotalBalance()).isEqualTo(BALANCE);
    }
}
