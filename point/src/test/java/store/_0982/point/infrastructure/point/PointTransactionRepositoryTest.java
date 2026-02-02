package store._0982.point.infrastructure.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import store._0982.point.domain.constant.PointType;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.support.BaseIntegrationTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PointTransactionRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @Test
    @DisplayName("멤버 ID와 타입(PAID)으로 트랜잭션을 조회한다")
    void findByMemberIdAndType_Paid() {
        // given
        UUID memberId = UUID.randomUUID();

        // 유상 포인트 충전 (PAID > 0)
        pointTransactionRepository.save(PointTransaction.charged(
                memberId, UUID.randomUUID(), PointAmount.paid(1000)
        ));

        // 보너스 적립 (PAID = 0, BONUS > 0)
        pointTransactionRepository.save(PointTransaction.bonusEarned(
                memberId, null, UUID.randomUUID(), PointAmount.bonus(500)
        ));

        // 다른 멤버의 트랜잭션
        pointTransactionRepository.save(PointTransaction.charged(
                UUID.randomUUID(), UUID.randomUUID(), PointAmount.paid(1000)
        ));

        // when
        Page<PointTransaction> result = pointTransactionRepository.findAllByMemberIdAndType(
                memberId, PointType.PAID, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPaidAmount()).isEqualTo(1000);
    }

    @Test
    @DisplayName("멤버 ID와 타입(BONUS)으로 트랜잭션을 조회한다")
    void findByMemberIdAndType_Bonus() {
        // given
        UUID memberId = UUID.randomUUID();

        // 유상 포인트 충전 (BONUS = 0)
        pointTransactionRepository.save(PointTransaction.charged(
                memberId, UUID.randomUUID(), PointAmount.paid(1000)
        ));

        // 보너스 적립 (BONUS > 0)
        pointTransactionRepository.save(PointTransaction.bonusEarned(
                memberId, null, UUID.randomUUID(), PointAmount.bonus(500)
        ));

        // when
        Page<PointTransaction> result = pointTransactionRepository.findAllByMemberIdAndType(
                memberId, PointType.BONUS, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBonusAmount()).isEqualTo(500);
    }

    @Test
    @DisplayName("멤버 ID와 타입(null)으로 전체 트랜잭션을 조회한다")
    void findByMemberIdAndType_All() {
        // given
        UUID memberId = UUID.randomUUID();

        pointTransactionRepository.save(PointTransaction.charged(
                memberId, UUID.randomUUID(), PointAmount.paid(1000)
        ));

        pointTransactionRepository.save(PointTransaction.bonusEarned(
                memberId, null, UUID.randomUUID(), PointAmount.bonus(500)
        ));

        // when
        Page<PointTransaction> result = pointTransactionRepository.findAllByMemberIdAndType(
                memberId, null, PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(2);
    }
}
