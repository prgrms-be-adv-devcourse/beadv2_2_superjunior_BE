package store._0982.point.application.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.bonus.BonusDeductCommand;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.exception.CustomErrorCode;
import store._0982.point.infrastructure.bonus.BonusDeductionJpaRepository;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.support.BaseIntegrationTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BonusDeductionServiceTest extends BaseIntegrationTest {

    @Autowired
    private BonusDeductionService bonusDeductionService;

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
    @DisplayName("보너스 포인트 차감: 유효기간이 임박한 순서대로 차감된다")
    void deductBonus_fifo_expiration() {
        // given
        // 1. 5일 남은 포인트 1000원
        BonusEarning earlyExpiring = createBonus(OffsetDateTime.now().plusDays(5));
        // 2. 10일 남은 포인트 1000원
        BonusEarning lateExpiring = createBonus(OffsetDateTime.now().plusDays(10));
        
        bonusEarningRepository.saveAll(List.of(lateExpiring, earlyExpiring)); // 순서 섞어서 저장

        // when
        long deductAmount = 1500L;
        BonusDeductCommand command = new BonusDeductCommand(transactionId, deductAmount);
        bonusDeductionService.deductBonus(memberId, command);

        // then
        List<BonusEarning> earnings = bonusEarningRepository.findAll();
        BonusEarning updatedEarly = earnings.stream().filter(e -> e.getId().equals(earlyExpiring.getId())).findFirst().orElseThrow();
        BonusEarning updatedLate = earnings.stream().filter(e -> e.getId().equals(lateExpiring.getId())).findFirst().orElseThrow();

        // 먼저 만료되는 포인트는 전액 사용됨
        assertThat(updatedEarly.getRemainingAmount()).isZero();
        assertThat(updatedEarly.getStatus()).isEqualTo(BonusEarningStatus.FULLY_USED);

        // 나중에 만료되는 포인트는 일부 사용됨
        assertThat(updatedLate.getRemainingAmount()).isEqualTo(500L); // 1000 - 500
        assertThat(updatedLate.getStatus()).isEqualTo(BonusEarningStatus.PARTIALLY_USED);

        // 차감 내역 생성 확인
        List<BonusDeduction> deductions = bonusDeductionRepository.findAll();
        assertThat(deductions).hasSize(2);
    }

    @Test
    @DisplayName("보너스 포인트 차감: 보유 포인트보다 많은 금액 차감 시 예외 발생")
    void deductBonus_insufficient_balance() {
        // given
        createBonus(OffsetDateTime.now().plusDays(5));

        // when & then
        BonusDeductCommand command = new BonusDeductCommand(transactionId, 2000L);
        assertThatThrownBy(() -> bonusDeductionService.deductBonus(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
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
