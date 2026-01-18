package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.repository.BonusDeductionRepository;
import store._0982.point.domain.repository.BonusEarningRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusRefundService {

    private final BonusDeductionRepository bonusDeductionRepository;
    private final BonusEarningRepository bonusEarningRepository;

    // TODO: 환불 로직 쿼리 횟수 개선 + 환불 사유에 따라 최소 보장 기간 여부 분기 필요
    @ServiceLog
    @RetryableTransactional
    public void refundBonus(UUID transactionId) {
        List<BonusDeduction> deductions = bonusDeductionRepository.findByTransactionId(transactionId);

        for (BonusDeduction deduction : deductions) {
            BonusEarning earning = bonusEarningRepository.findById(deduction.getBonusEarningId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

            earning.refund(deduction.getAmount());
            bonusEarningRepository.save(earning);
        }
    }
}