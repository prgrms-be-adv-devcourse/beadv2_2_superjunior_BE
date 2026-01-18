package store._0982.point.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PointTransactionTest {

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트 사용 내역을 생성한다")
    void createUsedHistory() {
        // when
        PointTransaction history = PointTransaction.used(
                memberId, orderId, idempotencyKey, PointAmount.of(5000, 0));

        // then
        assertThat(history).isNotNull();
        assertThat(history.getMemberId()).isEqualTo(memberId);
        assertThat(history.getOrderId()).isEqualTo(orderId);
        assertThat(history.getTotalAmount()).isEqualTo(5000);
        assertThat(history.getStatus()).isEqualTo(PointTransactionStatus.USED);
        assertThat(history.getIdempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    @DisplayName("포인트 반환 내역을 생성한다")
    void createReturnedHistory() {
        // when
        PointTransaction history = PointTransaction.returned(
                memberId, orderId, idempotencyKey, PointAmount.of(3000, 0), "테스트");

        // then
        assertThat(history).isNotNull();
        assertThat(history.getMemberId()).isEqualTo(memberId);
        assertThat(history.getOrderId()).isEqualTo(orderId);
        assertThat(history.getTotalAmount()).isEqualTo(3000);
        assertThat(history.getStatus()).isEqualTo(PointTransactionStatus.RETURNED);
        assertThat(history.getIdempotencyKey()).isEqualTo(idempotencyKey);
    }

    @Test
    @DisplayName("포인트 사용 내역과 반환 내역은 상태가 다르다")
    void differentStatus() {
        // given
        UUID orderId2 = UUID.randomUUID();
        UUID idempotencyKey2 = UUID.randomUUID();

        // when
        PointTransaction usedHistory = PointTransaction.used(
                memberId, orderId, idempotencyKey, PointAmount.of(5000, 0));
        PointTransaction returnedHistory = PointTransaction.returned(
                memberId, orderId2, idempotencyKey2, PointAmount.of(3000, 0), "테스트");

        // then
        assertThat(usedHistory.getStatus()).isEqualTo(PointTransactionStatus.USED);
        assertThat(returnedHistory.getStatus()).isEqualTo(PointTransactionStatus.RETURNED);
        assertThat(usedHistory.getStatus()).isNotEqualTo(returnedHistory.getStatus());
    }
}
