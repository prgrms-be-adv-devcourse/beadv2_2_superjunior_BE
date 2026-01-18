package store._0982.point.application.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.infrastructure.bonus.BonusDeductionJpaRepository;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.support.BaseIntegrationTest;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BonusRefundServiceTest extends BaseIntegrationTest {

    @Autowired
    private BonusRefundService bonusRefundService;

    @Autowired
    private BonusEarningJpaRepository bonusEarningRepository;

    @Autowired
    private BonusDeductionJpaRepository bonusDeductionRepository;

    private UUID memberId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        bonusDeductionRepository.deleteAll();
        bonusEarningRepository.deleteAll();
        memberId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("보너스 환불: 차감된 포인트가 정상적으로 복구된다")
    void refundBonus_success() {
        // given
        // 1000원 적립 -> 500원 사용 -> 잔액 500원
        BonusEarning earning = createBonus(OffsetDateTime.now().plusDays(10));
        earning.deduct(500L);
        bonusEarningRepository.save(earning);

        BonusDeduction deduction = BonusDeduction.create(earning.getId(), transactionId, 500L);
        bonusDeductionRepository.save(deduction);

        // when
        bonusRefundService.refundBonus(transactionId);

        // then
        BonusEarning refundedEarning = bonusEarningRepository.findById(earning.getId()).orElseThrow();
        assertThat(refundedEarning.getRemainingAmount()).isEqualTo(1000L);
        assertThat(refundedEarning.getStatus()).isEqualTo(BonusEarningStatus.ACTIVE);
    }

    @ParameterizedTest
    @MethodSource("provideDates")
    @DisplayName("보너스 환불: 만료일이 7일 미만인 경우 7일로 연장된다")
    void refundBonus_extend_expiration(OffsetDateTime expiresAt) {
        // given
        BonusEarning earning = createBonus(expiresAt);
        earning.deduct(1000L); // 전액 사용
        bonusEarningRepository.save(earning);

        BonusDeduction deduction = BonusDeduction.create(earning.getId(), transactionId, 1000L);
        bonusDeductionRepository.save(deduction);

        // when
        bonusRefundService.refundBonus(transactionId);

        // then
        BonusEarning refundedEarning = bonusEarningRepository.findById(earning.getId()).orElseThrow();
        assertThat(refundedEarning.getRemainingAmount()).isEqualTo(1000L);
        assertThat(refundedEarning.getStatus()).isEqualTo(BonusEarningStatus.ACTIVE);
        
        // 만료일이 오늘 + 7일 이후인지 확인 (오차 감안하여 6일 23시간 이상으로 검증)
        assertThat(refundedEarning.getExpiresAt()).isAfter(OffsetDateTime.now().plusDays(6).plusHours(23));
    }

    private static Stream<Arguments> provideDates() {
        OffsetDateTime now = OffsetDateTime.now();
        return Stream.of(
                Arguments.of(now.minusDays(1)),
                Arguments.of(now.plusDays(1)),
                Arguments.of(now.plusDays(6))
        );
    }

    private BonusEarning createBonus(OffsetDateTime expiresAt) {
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
