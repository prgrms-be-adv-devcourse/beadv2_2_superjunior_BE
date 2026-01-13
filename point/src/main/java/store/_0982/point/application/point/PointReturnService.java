package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;
import store._0982.point.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointReturnService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @ServiceLog
    @Transactional
    public void returnPoints(UUID memberId, PointReturnCommand command) {
        PointBalance point = pointBalanceRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(CustomErrorCode.MEMBER_NOT_FOUND));

        long amount = command.amount();
        PointTransaction usedHistory = pointTransactionRepository.findByOrderIdAndStatus(command.orderId(), PointTransactionStatus.USED)
                .orElseThrow(() -> new EntityNotFoundException(CustomErrorCode.ORDER_NOT_FOUND));

        PointAmount refundAmount = usedHistory.getPointAmount().calculateRefund(amount);
        PointTransaction returned = PointTransaction.returned(
                memberId, command.orderId(), command.idempotencyKey(), refundAmount, command.cancelReason());

        returned = pointTransactionRepository.saveAndFlush(returned);
        point.charge(refundAmount.paidPoint());
        point.earnBonus(refundAmount.bonusPoint());
        applicationEventPublisher.publishEvent(PointReturnedEvent.from(returned));
    }
}
