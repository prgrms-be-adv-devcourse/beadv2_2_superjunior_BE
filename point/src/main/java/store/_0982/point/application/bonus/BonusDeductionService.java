package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.repository.BonusDeductionRepository;
import store._0982.point.domain.repository.BonusEarningRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusDeductionService {

    private final BonusEarningRepository bonusEarningRepository;
    private final BonusDeductionRepository bonusDeductionRepository;

    @ServiceLog
    @Transactional
    public void deductBonus(UUID memberId, UUID transactionId, long deductAmount) {
        List<BonusEarning> usableEarnings = bonusEarningRepository.findByMemberIdAndStatusInOrderByExpiresAtAsc(
                memberId,
                List.of(BonusEarningStatus.ACTIVE, BonusEarningStatus.PARTIALLY_USED)
        );

        List<BonusDeduction> deductions = processBonusDeduction(transactionId, deductAmount, usableEarnings);
        bonusDeductionRepository.saveAll(deductions);
    }

    private List<BonusDeduction> processBonusDeduction(UUID transactionId, long deductAmount, List<BonusEarning> usableEarnings) {
        long remainingDeductAmount = deductAmount;
        List<BonusDeduction> deductions = new ArrayList<>();

        for (BonusEarning earning : usableEarnings) {
            if (remainingDeductAmount <= 0) {
                break;
            }

            long deductedAmount = earning.deduct(remainingDeductAmount);
            BonusDeduction deduction = BonusDeduction.create(earning.getId(), transactionId, deductedAmount);
            deductions.add(deduction);

            remainingDeductAmount -= deductedAmount;
        }

        // 데이터 정합성이 맞지 않을 경우 예외 발생
        if (remainingDeductAmount > 0) {
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }
        return deductions;
    }
}
