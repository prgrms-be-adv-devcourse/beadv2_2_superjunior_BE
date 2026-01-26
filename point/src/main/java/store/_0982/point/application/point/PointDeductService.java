package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.bonus.BonusDeductionService;
import store._0982.point.application.dto.bonus.BonusDeductCommand;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointDeductedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointDeductService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BonusDeductionService bonusDeductionService;

    @ServiceLog
    @RetryableTransactional
    public PointBalanceInfo processDeductionWithBonus(UUID memberId, PointDeductCommand command) {
        PointDeductResult result = deductPoints(memberId, command);

        PointTransaction transaction = result.transaction();
        if (result.deductedAmount().getBonusPoint() > 0) {
            BonusDeductCommand bonusDeductCommand = new BonusDeductCommand(transaction.getId(), result.deductedAmount().getBonusPoint());
            bonusDeductionService.deductBonus(memberId, bonusDeductCommand);
        }

        applicationEventPublisher.publishEvent(PointDeductedTxEvent.from(transaction));
        return PointBalanceInfo.from(result.updatedBalance());
    }

    private PointDeductResult deductPoints(UUID memberId, PointDeductCommand command) {
        UUID idempotencyKey = command.idempotencyKey();
        UUID orderId = command.orderId();
        long amount = command.amount();

        if (pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)) {
            throw new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST);
        }

        PointBalance balance = findPointBalanceForDeduction(memberId, amount);
        PointAmount deductionDelta = processDeduction(balance, amount);
        PointTransaction used = recordPointTransaction(memberId, orderId, idempotencyKey, deductionDelta, command.groupPurchaseName());

        return new PointDeductResult(balance, used, deductionDelta);
    }

    private PointBalance findPointBalanceForDeduction(UUID memberId, long amount) {
        PointBalance point = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        point.validateDeductible(amount);
        return point;
    }

    private static PointAmount processDeduction(PointBalance balance, long amount) {
        PointAmount deductionDelta = balance.calculateDeduction(amount);
        balance.use(amount);
        return deductionDelta;
    }

    private PointTransaction recordPointTransaction(UUID memberId, UUID orderId, UUID idempotencyKey, PointAmount deductionDelta, String groupPurchaseName) {
        PointTransaction used = PointTransaction.used(memberId, orderId, idempotencyKey, deductionDelta, groupPurchaseName);
        pointTransactionRepository.saveAndFlush(used);
        return used;
    }

    private record PointDeductResult(
            PointBalance updatedBalance,
            PointTransaction transaction,
            PointAmount deductedAmount
    ) {
    }
}
