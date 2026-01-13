package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PointTransactionTest {

    @Test
    @DisplayName("포인트 사용 내역을 생성한다")
    void createUsedHistory() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

        // when
        PointTransaction history = PointTransaction.used(memberId, command);

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
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, 3000);

        // when
        PointTransaction history = PointTransaction.returned(memberId, command);

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
        UUID memberId = UUID.randomUUID();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID idempotencyKey1 = UUID.randomUUID();
        UUID idempotencyKey2 = UUID.randomUUID();
        PointDeductCommand deductCommand = new PointDeductCommand(idempotencyKey1, orderId1, 5000);
        PointReturnCommand returnCommand = new PointReturnCommand(idempotencyKey2, orderId2, 3000);

        // when
        PointTransaction usedHistory = PointTransaction.used(memberId, deductCommand);
        PointTransaction returnedHistory = PointTransaction.returned(memberId, returnCommand);

        // then
        assertThat(usedHistory.getStatus()).isEqualTo(PointTransactionStatus.USED);
        assertThat(returnedHistory.getStatus()).isEqualTo(PointTransactionStatus.RETURNED);
        assertThat(usedHistory.getStatus()).isNotEqualTo(returnedHistory.getStatus());
    }
}
