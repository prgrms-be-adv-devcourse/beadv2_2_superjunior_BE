package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.bonus.BonusRefundService;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointReturnedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointReturnService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BonusRefundService bonusRefundService;

    @ServiceLog
    @RetryableTransactional
    public void returnPoints(UUID memberId, PointReturnCommand command) {
        if (pointTransactionRepository.existsByIdempotencyKey(command.idempotencyKey())) {
            throw new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST);
        }

        PointBalance point = findPointBalance(memberId);
        PointTransaction usedHistory = findUsedTransaction(command.orderId());
        PointReturnResult result = processRefund(memberId, command, usedHistory, point);

        if (result.refundAmount().getBonusPoint() > 0) {
            bonusRefundService.refundBonus(usedHistory.getId());
        }

        applicationEventPublisher.publishEvent(PointReturnedTxEvent.from(result.returned()));
    }

    private PointBalance findPointBalance(UUID memberId) {
        return pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }

    private PointTransaction findUsedTransaction(UUID orderId) {
        return pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
    }

    private PointReturnResult processRefund(UUID memberId, PointReturnCommand command, PointTransaction usedHistory, PointBalance point) {
        PointAmount refundAmount = (command.amount() == null) ?
                usedHistory.getPointAmount() : usedHistory.calculateRefund(command.amount());

        PointTransaction returned = PointTransaction.returned(
                memberId, command.orderId(), command.idempotencyKey(), refundAmount, command.cancelReason());

        returned = pointTransactionRepository.saveAndFlush(returned);
        point.addAmount(refundAmount);
        return new PointReturnResult(refundAmount, returned);
    }

    private record PointReturnResult(PointAmount refundAmount, PointTransaction returned) {
    }
}
