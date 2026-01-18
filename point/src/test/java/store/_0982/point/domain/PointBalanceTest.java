package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointBalanceTest {

    @Test
    @DisplayName("회원 포인트를 생성한다")
    void create() {
        // given
        UUID memberId = UUID.randomUUID();

        // when
        PointBalance pointBalance = new PointBalance(memberId);

        // then
        assertThat(pointBalance.getMemberId()).isEqualTo(memberId);
        assertThat(pointBalance.getTotalBalance()).isZero();
        assertThat(pointBalance.getLastUsedAt()).isNull();
    }

    @Test
    @DisplayName("포인트를 추가한다")
    void charge() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);

        // when
        pointBalance.charge(10000);

        // then
        assertThat(pointBalance.getTotalBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("포인트를 여러 번 추가한다")
    void chargeMultipleTimes() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);

        // when
        pointBalance.charge(5000);
        pointBalance.charge(3000);
        pointBalance.charge(2000);

        // then
        assertThat(pointBalance.getTotalBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("포인트를 차감한다")
    void use() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(10000);

        // when
        pointBalance.use(5000);

        // then
        assertThat(pointBalance.getTotalBalance()).isEqualTo(5000);
        assertThat(pointBalance.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("포인트 차감 시 잔액이 부족하면 예외가 발생한다")
    void use_fail_whenInsufficientBalance() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        // when & then
        assertThatThrownBy(() -> pointBalance.use(10000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
    }

    @Test
    @DisplayName("포인트를 환불한다")
    void deduct() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(10000);

        // when
        pointBalance.transfer(3000);

        // then
        assertThat(pointBalance.getTotalBalance()).isEqualTo(7000);
    }

    @Test
    @DisplayName("포인트 환불 시 잔액이 부족하면 예외가 발생한다")
    void deduct_fail_whenInsufficientBalance() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        // when & then
        assertThatThrownBy(() -> pointBalance.transfer(10000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
    }

    @Test
    @DisplayName("0 포인트 차감은 성공한다")
    void use_zero() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(10000);

        // when
        pointBalance.use(0);

        // then
        assertThat(pointBalance.getTotalBalance()).isEqualTo(10000);
        assertThat(pointBalance.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("0 포인트 추가는 성공한다")
    void charge_zero() {
        // given
        UUID memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);

        // when
        pointBalance.charge(0);

        // then
        assertThat(pointBalance.getTotalBalance()).isZero();
    }
}
