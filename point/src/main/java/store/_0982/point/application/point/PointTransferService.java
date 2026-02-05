package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointTransferCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointTransferredTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointTransferService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @ServiceLog
    @RetryableTransactional
    public PointBalanceInfo transfer(UUID memberId, PointTransferCommand command) {
        long amount = command.amount();
        PointBalance balance = findPointBalanceForTransfer(memberId, amount);

        PointTransaction transferred = PointTransaction.transferred(memberId, command.idempotencyKey(), PointAmount.paid(amount));
        transferred = pointTransactionRepository.saveAndFlush(transferred);
        balance.transfer(amount);

        applicationEventPublisher.publishEvent(PointTransferredTxEvent.from(transferred));
        return PointBalanceInfo.from(balance);
    }

    private PointBalance findPointBalanceForTransfer(UUID memberId, long amount) {
        PointBalance point =  pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        point.validateWithdrawable(amount);
        return point;
    }
}
