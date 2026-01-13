package store._0982.point.application;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.application.point.PointTransactionService;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.infrastructure.PointTransactionJpaRepository;
import store._0982.point.infrastructure.PointBalanceJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class PointTransactionServiceConcurrencyTest extends BaseConcurrencyTest {

    private static final int BALANCE = 100_000;
    private static final int AMOUNT = 1_000;

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private PointTransactionService pointTransactionService;

    @Autowired
    private PointBalanceJpaRepository memberPointRepository;

    @Autowired
    private PointTransactionJpaRepository memberPointHistoryRepository;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberPointRepository.deleteAll();
        memberPointHistoryRepository.deleteAll();

        memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(BALANCE);
        memberPointRepository.save(pointBalance);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 차감 요청을 중복 처리하지 않는다")
    void concurrent_use_idempotent() throws InterruptedException {
        // given
        PointDeductCommand command = new PointDeductCommand(UUID.randomUUID(), UUID.randomUUID(), AMOUNT);
        OrderInfo orderInfo = new OrderInfo(command.orderId(), command.amount(), OrderInfo.Status.CANCELLED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTask(() -> pointTransactionService.deductPoints(memberId, command));

        // then
        validate(true);
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

        OrderInfo orderInfo = new OrderInfo(orderId, AMOUNT, OrderInfo.Status.ORDER_FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTasks(commands, command -> pointTransactionService.deductPoints(memberId, command));

        // then
        validate(true);
    }

    @Test
    @DisplayName("네트워크 장애에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_idempotent() throws InterruptedException {
        // given
        PointReturnCommand command = new PointReturnCommand(UUID.randomUUID(), UUID.randomUUID(), AMOUNT);
        OrderInfo orderInfo = new OrderInfo(command.orderId(), command.amount(), OrderInfo.Status.ORDER_FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTask(() -> pointTransactionService.returnPoints(memberId, command));

        // then
        validate(false);
    }

    @Test
    @DisplayName("사용자 실수에 의한 중복 반환 요청을 중복 처리하지 않는다")
    void concurrent_return_duplicate() throws InterruptedException {
        // given
        UUID orderId = UUID.randomUUID();
        List<PointReturnCommand> commands = FIXTURE_MONKEY.giveMeBuilder(PointReturnCommand.class)
                .instantiate(Instantiator.constructor())
                .set("amount", AMOUNT)
                .set("orderId", orderId)
                .setNotNull("idempotencyKey")
                .sampleList(getDefaultThreadCount());

        OrderInfo orderInfo = new OrderInfo(orderId, AMOUNT, OrderInfo.Status.ORDER_FAILED, memberId, 1);

        when(orderServiceClient.getOrder(any(UUID.class), eq(memberId))).thenReturn(orderInfo);
        doNothing().when(applicationEventPublisher).publishEvent(any());

        // when
        runSynchronizedTasks(commands, command -> pointTransactionService.returnPoints(memberId, command));

        // then
        validate(false);
    }

    private void validate(boolean isDeduct) {
        assertThat(memberPointHistoryRepository.count()).isEqualTo(1);
        PointBalance pointBalance = memberPointRepository.findById(memberId).orElseThrow();
        assertThat(pointBalance.getTotalBalance()).isEqualTo(isDeduct ? BALANCE - AMOUNT : BALANCE + AMOUNT);
    }
}
