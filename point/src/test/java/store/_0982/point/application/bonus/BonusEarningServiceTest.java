package store._0982.point.application.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.entity.BonusPolicy;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.infrastructure.bonus.BonusPolicyJpaRepository;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseIntegrationTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BonusEarningServiceTest extends BaseIntegrationTest {

    @Autowired
    private BonusEarningService bonusEarningService;

    @Autowired
    private PointBalanceJpaRepository pointBalanceRepository;

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @Autowired
    private BonusPolicyJpaRepository bonusPolicyRepository;

    @Autowired
    private BonusEarningJpaRepository bonusEarningRepository;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        bonusEarningRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        pointBalanceRepository.deleteAll();
        bonusPolicyRepository.deleteAll();

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        PointBalance pointBalance = new PointBalance(memberId);
        pointBalanceRepository.save(pointBalance);
    }

    @Test
    @DisplayName("정상 적립: 유효한 정책이 있으면 포인트가 적립된다 (정률)")
    void earnBonus_success() {
        // given
        long paidAmount = 100_000L;
        BonusPolicy policy = createRatePolicy("10% 적립", 0.1, 50_000L);
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, paidAmount);
        BonusEarnCommand command = new BonusEarnCommand(UUID.randomUUID(), orderId, null, null);

        // when
        bonusEarningService.processBonus(memberId, command);

        // then
        PointBalance updatedBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(updatedBalance.getTotalBalance()).isEqualTo(10_000L);

        assertThat(bonusEarningRepository.findAll()).singleElement()
                .satisfies(earning -> {
                    assertThat(earning.getAmount()).isEqualTo(10_000L);
                    assertThat(earning.getPolicyId()).isEqualTo(policy.getId());
                });
    }

    @Test
    @DisplayName("한도 초과: 계산된 포인트가 최대 한도를 넘으면 한도까지만 적립된다")
    void earnBonus_capped_by_max_reward() {
        // given
        long paidAmount = 1_000_000L;
        BonusPolicy policy = createRatePolicy("10% 적립, 최대 5만원", 0.1, 50_000L);
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, paidAmount);
        BonusEarnCommand command = new BonusEarnCommand(UUID.randomUUID(), orderId);

        // when
        bonusEarningService.processBonus(memberId, command);

        // then
        PointBalance updatedBalance = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(updatedBalance.getTotalBalance()).isEqualTo(50_000L);
    }

    @Test
    @DisplayName("최소 구매 금액 미달: 조건을 만족하지 않으면 적립되지 않는다")
    void earnBonus_fail_below_min_purchase() {
        // given
        long paidAmount = 10_000L;
        BonusPolicy policy = BonusPolicy.builder()
                .name("5만원 이상 적립")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.1))
                .minPurchaseAmount(50_000L)
                .maxRewardAmount(100_000L)
                .isActive(true)
                .expirationDays(365)
                .build();
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, paidAmount);
        BonusEarnCommand command = new BonusEarnCommand(UUID.randomUUID(), orderId);

        // when
        bonusEarningService.processBonus(memberId, command);

        // then
        assertThat(bonusEarningRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("기간 만료: 유효 기간이 지난 정책은 적용되지 않는다")
    void earnBonus_fail_expired_policy() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("지난 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.1))
                .maxRewardAmount(100_000L)
                .validFrom(OffsetDateTime.now().minusDays(10))
                .validUntil(OffsetDateTime.now().minusDays(1))
                .isActive(true)
                .expirationDays(365)
                .build();
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, 100_000L);
        bonusEarningService.processBonus(memberId, new BonusEarnCommand(UUID.randomUUID(), orderId));

        // then
        assertThat(bonusEarningRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("상시 정책: 기간 설정이 없는(null) 정책은 항상 적용된다")
    void earnBonus_success_always_valid_policy() {
        // given
        BonusPolicy policy = createRatePolicy("상시 혜택", 0.01, 100_000L);
        // validFrom, validUntil은 null 기본값
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, 100_000L);
        bonusEarningService.processBonus(memberId, new BonusEarnCommand(UUID.randomUUID(), orderId));

        // then
        assertThat(bonusEarningRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("비활성 정책: active=false인 정책은 적용되지 않는다")
    void earnBonus_fail_inactive_policy() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("중단된 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.5))
                .maxRewardAmount(1_000_000L)
                .isActive(false)
                .expirationDays(365)
                .build();
        bonusPolicyRepository.save(policy);

        createUsedTransaction(memberId, orderId, 100_000L);
        bonusEarningService.processBonus(memberId, new BonusEarnCommand(UUID.randomUUID(), orderId));

        // then
        assertThat(bonusEarningRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("우선순위: 여러 정률 정책 중 적립률이 가장 높은 정책 하나만 적용된다")
    void earnBonus_best_rate_priority() {
        // given
        long paidAmount = 100_000L;
        BonusPolicy policyLow = createRatePolicy("5% 적립", 0.05, 100_000L);
        BonusPolicy policyHigh = createRatePolicy("10% 적립", 0.10, 100_000L);
        bonusPolicyRepository.saveAll(java.util.List.of(policyLow, policyHigh));

        createUsedTransaction(memberId, orderId, paidAmount);
        BonusEarnCommand command = new BonusEarnCommand(UUID.randomUUID(), orderId);

        // when
        bonusEarningService.processBonus(memberId, command);

        // then
        assertThat(bonusEarningRepository.findAll()).hasSize(1)
                .first()
                .satisfies(earning -> {
                    assertThat(earning.getAmount()).isEqualTo(10_000L); // 10% 정책 적용
                    assertThat(earning.getPolicyId()).isEqualTo(policyHigh.getId());
                });
    }

    private BonusPolicy createRatePolicy(String name, double rate, long maxReward) {
        return BonusPolicy.builder()
                .name(name)
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(rate))
                .maxRewardAmount(maxReward)
                .isActive(true)
                .expirationDays(365)
                .build();
    }

    private void createUsedTransaction(UUID memberId, UUID orderId, long amount) {
        PointTransaction tx = PointTransaction.used(
                memberId, orderId, UUID.randomUUID(), PointAmount.paid(amount));
        pointTransactionRepository.save(tx);
    }
}
