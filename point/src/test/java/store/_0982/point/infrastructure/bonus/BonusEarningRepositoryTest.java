package store._0982.point.infrastructure.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.support.BaseIntegrationTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BonusEarningRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private BonusEarningJpaRepository bonusEarningRepository;

    @BeforeEach
    void setUp() {
        bonusEarningRepository.deleteAll();
    }

    @Test
    @DisplayName("사용 가능한 보너스 포인트를 유효기간 오름차순으로 조회한다")
    void findByMemberIdAndStatusInOrderByExpiresAtAsc() {
        // given
        UUID memberId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        // 1. 만료된 포인트 (조회되면 안됨)
        BonusEarning expired = createBonus(memberId, now.minusDays(1));
        expired.markExpired();

        // 2. 사용 완료된 포인트 (조회되면 안됨)
        BonusEarning fullyUsed = createBonus(memberId, now.plusDays(10));
        fullyUsed.markFullyUsed();

        // 3. 사용 가능한 포인트 - 유효기간 5일 남음 (두 번째로 조회되어야 함)
        BonusEarning activeLater = createBonus(memberId, now.plusDays(5));

        // 4. 사용 가능한 포인트 - 유효기간 1일 남음 (첫 번째로 조회되어야 함)
        BonusEarning activeSooner = createBonus(memberId, now.plusDays(1));

        // 5. 부분 사용된 포인트 - 유효기간 10일 남음 (세 번째로 조회되어야 함)
        BonusEarning partiallyUsed = createBonus(memberId, now.plusDays(10));
        partiallyUsed.markPartiallyUsed();

        bonusEarningRepository.saveAllAndFlush(List.of(expired, fullyUsed, activeLater, activeSooner, partiallyUsed));

        // when
        List<BonusEarning> results = bonusEarningRepository.findByMemberIdAndStatusInOrderByExpiresAtAsc(
                memberId,
                List.of(BonusEarningStatus.ACTIVE, BonusEarningStatus.PARTIALLY_USED)
        );

        // then
        assertThat(results).hasSize(3)
                .extracting(BonusEarning::getId)
                .containsExactly(activeSooner.getId(), activeLater.getId(), partiallyUsed.getId());
    }

    private BonusEarning createBonus(UUID memberId, OffsetDateTime expiresAt) {
        return BonusEarning.earned(
                memberId,
                1000L,
                BonusEarningType.PURCHASE_REWARD,
                expiresAt,
                UUID.randomUUID(),
                "테스트 적립"
        );
    }
}
