package store._0982.point.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BonusEarningTest {

    @Test
    @DisplayName("보너스 적립 내역을 생성한다")
    void earned() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        long amount = 5000;
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        // when
        BonusEarning earning = BonusEarning.earned(
                memberId,
                amount,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );

        // then
        assertThat(earning.getMemberId()).isEqualTo(memberId);
        assertThat(earning.getAmount()).isEqualTo(amount);
        assertThat(earning.getRemainingAmount()).isEqualTo(amount);
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.ACTIVE);
        assertThat(earning.getType()).isEqualTo(BonusEarningType.PURCHASE_REWARD);
    }

    @Test
    @DisplayName("주문 완료 시 보너스 적립 내역을 생성한다")
    void fromOrder() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        long amount = 3000;
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        // when
        BonusEarning earning = BonusEarning.fromOrder(
                memberId,
                amount,
                orderId,
                expiresAt,
                policyId,
                "주문 완료 적립"
        );

        // then
        assertThat(earning.getOrderId()).isEqualTo(orderId);
        assertThat(earning.getType()).isEqualTo(BonusEarningType.PURCHASE_REWARD);
    }

    @Test
    @DisplayName("보너스 포인트를 차감한다 - 일부 사용")
    void deduct_partiallyUsed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );

        // when
        long deductedAmount = earning.deduct(2000);

        // then
        assertThat(deductedAmount).isEqualTo(2000);
        assertThat(earning.getRemainingAmount()).isEqualTo(3000);
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.PARTIALLY_USED);
    }

    @Test
    @DisplayName("보너스 포인트를 차감한다 - 전액 사용")
    void deduct_fullyUsed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );

        // when
        long deductedAmount = earning.deduct(5000);

        // then
        assertThat(deductedAmount).isEqualTo(5000);
        assertThat(earning.getRemainingAmount()).isZero();
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.FULLY_USED);
    }

    @Test
    @DisplayName("보너스 포인트 차감 시 잔액보다 많은 금액을 요청하면 잔액만큼만 차감한다")
    void deduct_exceedsRemaining() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                3000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );

        // when
        long deductedAmount = earning.deduct(5000);

        // then
        assertThat(deductedAmount).isEqualTo(3000);  // 잔액만큼만 차감
        assertThat(earning.getRemainingAmount()).isZero();
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.FULLY_USED);
    }

    @Test
    @DisplayName("보너스 포인트를 환불한다")
    void refund() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.deduct(3000);  // 2000원 남음

        // when
        earning.refund(2000);

        // then
        assertThat(earning.getRemainingAmount()).isEqualTo(4000);
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.PARTIALLY_USED);
    }

    @Test
    @DisplayName("보너스 포인트 전액 환불 시 상태가 ACTIVE로 변경된다")
    void refund_fullyRestored() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.deduct(5000);  // 전액 사용

        // when
        earning.refund(5000);

        // then
        assertThat(earning.getRemainingAmount()).isEqualTo(5000);
        assertThat(earning.getStatus()).isEqualTo(BonusEarningStatus.ACTIVE);
    }

    @Test
    @DisplayName("보너스 포인트 환불 시 원래 금액보다 많으면 예외가 발생한다")
    void refund_exceedsOriginalAmount() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.deduct(3000);

        // when & then
        assertThatThrownBy(() -> earning.refund(4000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.INVALID_REFUND_AMOUNT.getMessage());
    }

    @Test
    @DisplayName("보너스 포인트 환불 시 유효기간이 7일 미만이면 7일로 연장된다")
    void refund_extendsExpirationDate() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(3);  // 3일 후 만료

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.deduct(3000);

        // when
        earning.refund(2000);

        // then
        assertThat(earning.getExpiresAt()).isAfter(OffsetDateTime.now().plusDays(6));
    }

    @Test
    @DisplayName("만료된 보너스는 차감할 수 없다")
    void deduct_expiredBonus() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.markExpired();

        // when & then
        assertThatThrownBy(() -> earning.deduct(1000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.INVALID_BONUS_STATUS.getMessage());
    }

    @Test
    @DisplayName("전액 사용된 보너스는 추가 차감할 수 없다")
    void deduct_fullyUsedBonus() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(30);

        BonusEarning earning = BonusEarning.earned(
                memberId,
                5000,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                policyId,
                "구매 적립"
        );
        earning.markFullyUsed();

        // when & then
        assertThatThrownBy(() -> earning.deduct(1000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.INVALID_BONUS_STATUS.getMessage());
    }
}
