package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.bonus.BonusDeductionService;
import store._0982.point.application.bonus.BonusRefundService;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedTxEvent;
import store._0982.point.domain.event.PointDeductedTxEvent;
import store._0982.point.domain.event.PointReturnedTxEvent;
import store._0982.point.domain.event.PointTransferredTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTxManager {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BonusDeductionService bonusDeductionService;
    private final BonusRefundService bonusRefundService;

    public PointBalance findPointBalance(UUID memberId) {
        return pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }

    public PointBalance findPointBalanceForDeduction(UUID memberId, long amount) {
        PointBalance point = findPointBalance(memberId);
        point.validateDeductible(amount);
        return point;
    }

    public PointBalance findPointBalanceForTransfer(UUID memberId, long amount) {
        PointBalance point = findPointBalance(memberId);
        point.validateWithdrawable(amount);
        return point;
    }

    public PointTransaction findUsedTransaction(UUID orderId) {
        return pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
    }

    // TODO: 충전, 차감, 반환에서 멱등성, 유효성 검증 + 동시성 제어 필요
    @RetryableTransactional
    public PointBalance chargePoints(UUID memberId, UUID idempotencyKey, long amount) {
        PointBalance point = findPointBalance(memberId);

        PointAmount chargeAmount = PointAmount.of(amount, 0);
        PointTransaction charged = PointTransaction.charged(memberId, idempotencyKey, chargeAmount);
        charged = pointTransactionRepository.saveAndFlush(charged);

        point.charge(amount);
        applicationEventPublisher.publishEvent(PointChargedTxEvent.from(charged));

        return point;
    }

    @RetryableTransactional
    public PointBalance deductPoints(UUID memberId, UUID orderId, UUID idempotencyKey, long amount) {
        if (pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)) {
            throw new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST);
        }

        PointBalance point = findPointBalanceForDeduction(memberId, amount);

        // 차감될 금액(Delta) 계산
        PointAmount deductionDelta = point.getPointAmount().calculateDeduction(amount);
        
        // 잔액 업데이트
        point.use(amount);

        // 트랜잭션에는 차감된 금액(Delta)을 기록
        PointTransaction used = PointTransaction.used(memberId, orderId, idempotencyKey, deductionDelta);
        used = pointTransactionRepository.saveAndFlush(used);

        // 보너스 차감 상세 기록
        if (deductionDelta.getBonusPoint() > 0) {
            bonusDeductionService.deductBonus(memberId, used.getId(), deductionDelta.getBonusPoint());
        }

        applicationEventPublisher.publishEvent(PointDeductedTxEvent.from(used));

        return point;
    }

    @RetryableTransactional
    public void returnPoints(UUID memberId, UUID orderId, UUID idempotencyKey, long amount, String cancelReason) {
        if (pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST);
        }

        PointBalance point = findPointBalance(memberId);
        PointTransaction usedHistory = findUsedTransaction(orderId);

        PointAmount refundAmount = usedHistory.calculateRefund(amount);
        PointTransaction returned = PointTransaction.returned(
                memberId, orderId, idempotencyKey, refundAmount, cancelReason);

        returned = pointTransactionRepository.saveAndFlush(returned);
        point.charge(refundAmount.getPaidPoint());
        point.earnBonus(refundAmount.getBonusPoint());

        // 보너스 환불 상세 처리
        if (refundAmount.getBonusPoint() > 0) {
            // 사용 트랜잭션 ID로 보너스 차감 내역을 찾아 환불 처리
            bonusRefundService.refundBonus(usedHistory.getId());
        }

        applicationEventPublisher.publishEvent(PointReturnedTxEvent.from(returned));
    }

    @RetryableTransactional
    public PointBalance transfer(UUID memberId, UUID idempotencyKey, long amount) {
        PointBalance balance = findPointBalanceForTransfer(memberId, amount);

        PointTransaction transferred = PointTransaction.transferred(memberId, idempotencyKey, PointAmount.paid(amount));
        transferred = pointTransactionRepository.saveAndFlush(transferred);
        balance.transfer(amount);

        applicationEventPublisher.publishEvent(PointTransferredTxEvent.from(transferred));

        return balance;
    }
}
