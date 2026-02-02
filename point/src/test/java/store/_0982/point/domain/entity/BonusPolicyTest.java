package store._0982.point.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.BonusPolicyType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

class BonusPolicyTest {

    @Test
    @DisplayName("비율 기반 보너스 포인트를 계산한다")
    void calculateBonusAmount_withRewardRate() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("구매 적립 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.1))  // 10% 적립
                .maxRewardAmount(10000L)
                .minPurchaseAmount(0L)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .isActive(true)
                .build();

        // when
        OptionalLong bonusAmount = policy.calculateBonusAmount(50000);

        // then
        assertThat(bonusAmount).isPresent();
        assertThat(bonusAmount.getAsLong()).isEqualTo(5000);  // 50000 * 0.1 = 5000
    }

    @Test
    @DisplayName("비율 기반 보너스 계산 시 최대 한도를 적용한다")
    void calculateBonusAmount_withMaxRewardLimit() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("구매 적립 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.1))  // 10% 적립
                .maxRewardAmount(3000L)  // 최대 3000원
                .minPurchaseAmount(0L)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .isActive(true)
                .build();

        // when
        OptionalLong bonusAmount = policy.calculateBonusAmount(50000);

        // then
        assertThat(bonusAmount).isPresent();
        assertThat(bonusAmount.getAsLong()).isEqualTo(3000);  // 50000 * 0.1 = 5000 -> 최대 3000
    }

    @Test
    @DisplayName("고정 금액 보너스 포인트를 계산한다")
    void calculateBonusAmount_withFixedAmount() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("이벤트 축하 적립")
                .type(BonusPolicyType.EVENT_REWARD)
                .fixedAmount(5000L)  // 고정 5000원
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .isActive(true)
                .build();

        // when
        OptionalLong bonusAmount = policy.calculateBonusAmount(0);  // 구매 금액 무관

        // then
        assertThat(bonusAmount).isPresent();
        assertThat(bonusAmount.getAsLong()).isEqualTo(5000);
    }

    @Test
    @DisplayName("비율과 고정 금액이 모두 없으면 빈 값을 반환한다")
    void calculateBonusAmount_withNoRewardConfig() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("잘못된 정책")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(null)
                .fixedAmount(null)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .isActive(true)
                .build();

        // when
        OptionalLong bonusAmount = policy.calculateBonusAmount(50000);

        // then
        assertThat(bonusAmount).isEmpty();
    }

    @Test
    @DisplayName("소수점 이하는 버림 처리한다")
    void calculateBonusAmount_withDecimalTruncation() {
        // given
        BonusPolicy policy = BonusPolicy.builder()
                .name("구매 적립 이벤트")
                .type(BonusPolicyType.PURCHASE_REWARD)
                .rewardRate(BigDecimal.valueOf(0.035))  // 3.5% 적립
                .maxRewardAmount(10000L)
                .minPurchaseAmount(0L)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .expirationDays(30)
                .isActive(true)
                .build();

        // when
        OptionalLong bonusAmount = policy.calculateBonusAmount(1000);

        // then
        assertThat(bonusAmount).isPresent();
        assertThat(bonusAmount.getAsLong()).isEqualTo(35);  // 1000 * 0.035 = 35.0
    }
}
