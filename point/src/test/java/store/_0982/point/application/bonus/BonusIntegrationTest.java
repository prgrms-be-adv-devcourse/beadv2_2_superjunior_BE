package store._0982.point.application.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.application.dto.point.PointChargeCommand;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.application.point.PointChargeService;
import store._0982.point.application.point.PointDeductService;
import store._0982.point.application.point.PointReturnService;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.constant.BonusEarningType;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.infrastructure.bonus.BonusDeductionJpaRepository;
import store._0982.point.infrastructure.bonus.BonusEarningJpaRepository;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseIntegrationTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BonusIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PointChargeService pointChargeService;

    @Autowired
    private PointDeductService pointDeductService;

    @Autowired
    private PointReturnService pointReturnService;

    @Autowired
    private PointBalanceJpaRepository pointBalanceRepository;

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @Autowired
    private BonusEarningJpaRepository bonusEarningRepository;

    @Autowired
    private BonusDeductionJpaRepository bonusDeductionRepository;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        pointTransactionRepository.deleteAll();
        bonusDeductionRepository.deleteAll();
        bonusEarningRepository.deleteAll();
        pointBalanceRepository.deleteAll();

        memberId = UUID.randomUUID();
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalanceRepository.save(pointBalance);
    }

    @Test
    @DisplayName("통합 시나리오: 충전 -> 보너스 적립 -> 사용(차감) -> 환불")
    void full_scenario() {
        // 1. 10,000 포인트 충전
        pointChargeService.chargePoints(memberId, new PointChargeCommand(10000L, UUID.randomUUID()));

        // 2. 보너스 1,000 포인트 적립 (유효기간 5일)
        BonusEarning bonus = BonusEarning.earned(
                memberId,
                1000L,
                BonusEarningType.PURCHASE_REWARD,
                OffsetDateTime.now().plusDays(5),
                UUID.randomUUID(),
                "테스트 적립"
        );
        bonusEarningRepository.save(bonus);
        pointBalanceRepository.findByMemberId(memberId).ifPresent(p -> {
            p.earnBonus(1000L);
            pointBalanceRepository.save(p);
        });

        // 3. 5,000 포인트 사용 (결제)
        // 예상: 보너스 1,000 차감 + 충전포인트 4,000 차감
        UUID orderId = UUID.randomUUID();
        pointDeductService.processDeductionWithBonus(
                memberId,
                new PointDeductCommand(UUID.randomUUID(), orderId, 5000L, "테스트 공구")
        );

        // 검증: 포인트 잔액
        PointBalance afterDeduct = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(afterDeduct.getBonusBalance()).isZero();
        assertThat(afterDeduct.getPaidBalance()).isEqualTo(6000L);

        // 검증: 보너스 차감 내역
        List<BonusDeduction> deductions = bonusDeductionRepository.findAll();
        assertThat(deductions).hasSize(1);
        assertThat(deductions.get(0).getAmount()).isEqualTo(1000L);

        // 검증: 보너스 상태
        BonusEarning usedBonus = bonusEarningRepository.findAll().get(0);
        assertThat(usedBonus.getStatus()).isEqualTo(BonusEarningStatus.FULLY_USED);
        assertThat(usedBonus.getRemainingAmount()).isZero();

        // 4. 결제 취소 (환불)
        pointReturnService.returnPoints(
                memberId, new PointReturnCommand(UUID.randomUUID(), orderId, "단순 변심", 5000L));

        // 검증: 포인트 잔액 복구
        PointBalance afterRefund = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
        assertThat(afterRefund.getBonusBalance()).isEqualTo(1000L);
        assertThat(afterRefund.getPaidBalance()).isEqualTo(10000L);

        // 검증: 보너스 상태 복구
        BonusEarning refundedBonus = bonusEarningRepository.findAll().get(0);
        assertThat(refundedBonus.getStatus()).isEqualTo(BonusEarningStatus.ACTIVE);
        assertThat(refundedBonus.getRemainingAmount()).isEqualTo(1000L);
    }
}
