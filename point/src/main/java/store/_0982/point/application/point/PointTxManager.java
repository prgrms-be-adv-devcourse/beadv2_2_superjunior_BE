package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.common.RetryForTransactional;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedTxEvent;
import store._0982.point.domain.event.PointDeductedTxEvent;
import store._0982.point.domain.event.PointReturnedTxEvent;
import store._0982.point.domain.event.PointTransferredEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointTxManager {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
    @Transactional
    @RetryForTransactional
    public PointBalance chargePoints(UUID memberId, UUID idempotencyKey, long amount) {
        PointBalance point = findPointBalance(memberId);

        PointAmount chargeAmount = PointAmount.of(amount, 0);
        PointTransaction charged = PointTransaction.charged(memberId, idempotencyKey, chargeAmount);
        charged = pointTransactionRepository.saveAndFlush(charged);

        point.charge(amount);
        applicationEventPublisher.publishEvent(PointChargedTxEvent.from(charged));

        return point;
    }

    @Transactional
    @RetryForTransactional
    public PointBalance deductPoints(UUID memberId, UUID orderId, UUID idempotencyKey, long amount) {
        if (pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)) {
            throw new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST);
        }

        PointBalance point = findPointBalanceForDeduction(memberId, amount);

        PointAmount deduction = point.use(amount);
        PointTransaction used = PointTransaction.used(memberId, orderId, idempotencyKey, deduction);
        used = pointTransactionRepository.saveAndFlush(used);

        applicationEventPublisher.publishEvent(PointDeductedTxEvent.from(used));

        return point;
    }

    @Transactional
    @RetryForTransactional
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
            // 2. 환불 금액만큼 포인트 복구 (충전 포인트 우선 복구 로직에 따라 PointAmount에서 계산된 값 사용)
            point.charge(refundAmount.getPaidPoint());
            point.earnBonus(refundAmount.getBonusPoint());

        applicationEventPublisher.publishEvent(PointReturnedTxEvent.from(returned));
    }

    @Transactional
    @RetryForTransactional
    public PointBalance transfer(UUID memberId, UUID idempotencyKey, long amount) {
        PointBalance balance = findPointBalanceForTransfer(memberId, amount);

        PointTransaction transferred = PointTransaction.transferred(memberId, idempotencyKey, PointAmount.paid(amount));
        transferred = pointTransactionRepository.saveAndFlush(transferred);
        balance.transfer(amount);

        applicationEventPublisher.publishEvent(PointTransferredEvent.from(transferred));

        return balance;
    }
}
