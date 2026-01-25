package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseConcurrencyTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Disabled("임시 보류")
class PointConcurrencyWithBonusTest extends BaseConcurrencyTest {

    private static final String SAMPLE_PURCHASE_NAME = "테스트 공구";

    @Autowired
    private PointDeductService pointDeductService;

    @Autowired
    private PointBalanceJpaRepository pointBalanceRepository;

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @Autowired
    private BonusEarningJpaRepository bonusEarningRepository;

    @MockitoBean
    private CommerceServiceClient commerceServiceClient;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        pointBalanceRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        bonusEarningRepository.deleteAll();

        memberId = UUID.randomUUID();
    }

    @Test
    @DisplayName("서로 다른 주문으로 동시에 포인트를 차감하면 순차적으로 처리되어야 한다 (Lost Update 방지)")
    void concurrent_deduction_different_orders() throws InterruptedException {
        // given
        long initialBalance = 10_000L;
        long deductAmount = 1_000L;
        int threadCount = getDefaultThreadCount(); // 10

        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(initialBalance);
        pointBalanceRepository.save(pointBalance);

        List<PointDeductCommand> commands = IntStream.range(0, threadCount)
                .mapToObj(i -> {
                    UUID orderId = UUID.randomUUID();
                    OrderInfo orderInfo = OrderInfo.builder()
                            .orderId(orderId)
                            .price(deductAmount)
                            .quantity(1)
                            .memberId(memberId)
                            .status(OrderInfo.Status.PENDING)
                            .build();
                    when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

                    return new PointDeductCommand(UUID.randomUUID(), orderId, deductAmount, SAMPLE_PURCHASE_NAME);
                })
                .toList();

        // when
        runSynchronizedTasks(commands, command ->
                pointDeductService.processDeductionWithBonus(memberId, command)
        );

        // then
        PointBalance finalBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        // 10000 - (1000 * 10) = 0
        assertThat(finalBalance.getTotalBalance()).isZero();
    }

    @Test
    @DisplayName("동시에 보너스 포인트를 사용하면 보너스 잔액이 정확하게 차감되어야 한다")
    void concurrent_bonus_usage() throws InterruptedException {
        // given
        long initialBonus = 1_000L;
        long useAmount = 100L;
        int threadCount = getDefaultThreadCount();

        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.earnBonus(initialBonus);
        pointBalanceRepository.save(pointBalance);

        BonusEarning bonus = BonusEarning.earned(
                memberId, initialBonus, BonusEarningType.EVENT_REWARD,
                OffsetDateTime.now().plusDays(7), UUID.randomUUID(), "Test Bonus"
        );
        bonusEarningRepository.save(bonus);

        List<PointDeductCommand> commands = IntStream.range(0, threadCount)
                .mapToObj(i -> {
                    UUID orderId = UUID.randomUUID();
                    OrderInfo orderInfo = OrderInfo.builder()
                            .orderId(orderId)
                            .price(useAmount)
                            .quantity(1)
                            .memberId(memberId)
                            .status(OrderInfo.Status.PENDING)
                            .build();
                    when(commerceServiceClient.getOrder(orderId, memberId)).thenReturn(orderInfo);

                    return new PointDeductCommand(UUID.randomUUID(), orderId, useAmount, SAMPLE_PURCHASE_NAME);
                })
                .toList();

        // when
        runSynchronizedTasks(commands, command ->
                pointDeductService.processDeductionWithBonus(memberId, command)
        );

        // then
        PointBalance finalBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(finalBalance.getBonusBalance()).isZero();

        BonusEarning finalBonus = bonusEarningRepository.findById(bonus.getId()).orElseThrow();
        assertThat(finalBonus.getRemainingAmount()).isZero();
        assertThat(finalBonus.getStatus()).isEqualTo(BonusEarningStatus.FULLY_USED);
    }
}
