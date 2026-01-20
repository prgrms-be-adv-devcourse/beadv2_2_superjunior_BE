package store._0982.point.application.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.entity.BonusPolicy;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.infrastructure.bonus.BonusPolicyJpaRepository;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseKafkaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderConfirmedEventListenerTest extends BaseKafkaTest {

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
    private UUID groupPurchaseId;

    @BeforeEach
    void setUp() {
        bonusEarningRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        bonusPolicyRepository.deleteAll();
        pointBalanceRepository.deleteAll();

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        groupPurchaseId = UUID.randomUUID();
    }

    @Test
    @DisplayName("주문 확정 이벤트를 수신하면 포인트 적립 로직이 실행된다")
    void handleOrderConfirmedEvent() {
        // given
        long paidAmount = 50000;
        PointBalance balance = new PointBalance(memberId);
        pointBalanceRepository.save(balance);

        PointTransaction payment = PointTransaction.used(
                memberId,
                orderId,
                UUID.randomUUID(),
                PointAmount.paid(paidAmount)
        );
        pointTransactionRepository.save(payment);

        // 10% 적립 정책 생성
        BonusPolicy policy = BonusPolicy.builder()
                .name("구매 적립 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.1))
                .maxRewardAmount(10000L)
                .minPurchaseAmount(0L)
                .validFrom(OffsetDateTime.now().minusDays(30)) // 기간 넉넉하게
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .targetCategory("FOOD")
                .isActive(true)
                .build();
        bonusPolicyRepository.save(policy);

        OrderConfirmedEvent event = new OrderConfirmedEvent(
                orderId,
                memberId,
                groupPurchaseId,
                "상품명",
                OrderConfirmedEvent.ProductCategory.FOOD
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, event);

        // then
        awaitUntilAsserted(() -> {
            long expectedBonus = 5000; // 50000 * 0.1

            // 1. 포인트 잔액 증가 확인
            PointBalance current = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
            assertThat(current.getBonusBalance()).isEqualTo(expectedBonus);

            // 2. 적립 트랜잭션 생성 확인 (orderId와 상태 검증)
            List<PointTransaction> transactions = pointTransactionRepository.findAllByOrderId(orderId);
            assertThat(transactions)
                    .extracting(PointTransaction::getStatus)
                    .contains(PointTransactionStatus.BONUS_EARNED);

            // 3. 보너스 적립 내역 확인
            List<BonusEarning> earnings = bonusEarningRepository.findAll();
            assertThat(earnings).singleElement()
                    .extracting(BonusEarning::getAmount, BonusEarning::getPolicyId)
                    .containsExactly(expectedBonus, policy.getId());
        });
    }
}
