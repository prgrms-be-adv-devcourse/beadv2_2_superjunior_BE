package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberPointTest {

    @Test
    @DisplayName("회원 포인트를 생성한다")
    void create() {
        // given
        UUID memberId = UUID.randomUUID();

        // when
        MemberPoint memberPoint = new MemberPoint(memberId);

        // then
        assertThat(memberPoint.getMemberId()).isEqualTo(memberId);
        assertThat(memberPoint.getPointBalance()).isZero();
        assertThat(memberPoint.getLastUsedAt()).isNull();
    }

    @Test
    @DisplayName("포인트를 추가한다")
    void addPoints() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);

        // when
        memberPoint.addPoints(10000);

        // then
        assertThat(memberPoint.getPointBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("포인트를 여러 번 추가한다")
    void addPointsMultipleTimes() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);

        // when
        memberPoint.addPoints(5000);
        memberPoint.addPoints(3000);
        memberPoint.addPoints(2000);

        // then
        assertThat(memberPoint.getPointBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("포인트를 차감한다")
    void deductPoints() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(10000);

        // when
        memberPoint.deductPoints(5000);

        // then
        assertThat(memberPoint.getPointBalance()).isEqualTo(5000);
        assertThat(memberPoint.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("포인트 차감 시 잔액이 부족하면 예외가 발생한다")
    void deductPoints_fail_whenInsufficientBalance() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(5000);

        // when & then
        assertThatThrownBy(() -> memberPoint.deductPoints(10000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
    }

    @Test
    @DisplayName("포인트를 환불한다")
    void refund() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(10000);

        // when
        memberPoint.refund(3000);

        // then
        assertThat(memberPoint.getPointBalance()).isEqualTo(7000);
    }

    @Test
    @DisplayName("포인트 환불 시 잔액이 부족하면 예외가 발생한다")
    void refund_fail_whenInsufficientBalance() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(5000);

        // when & then
        assertThatThrownBy(() -> memberPoint.refund(10000))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
    }

    @Test
    @DisplayName("0 포인트 차감은 성공한다")
    void deductPoints_zero() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(10000);

        // when
        memberPoint.deductPoints(0);

        // then
        assertThat(memberPoint.getPointBalance()).isEqualTo(10000);
        assertThat(memberPoint.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("0 포인트 추가는 성공한다")
    void addPoints_zero() {
        // given
        UUID memberId = UUID.randomUUID();
        MemberPoint memberPoint = new MemberPoint(memberId);

        // when
        memberPoint.addPoints(0);

        // then
        assertThat(memberPoint.getPointBalance()).isZero();
    }
}
