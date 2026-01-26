package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointChargeCommand;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointChargeService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @ServiceLog
    @RetryableTransactional
    public PointBalanceInfo chargePoints(UUID memberId, PointChargeCommand command) {
        PointBalance balance = findPointBalance(memberId);
        long amount = command.amount();

        PointAmount chargeAmount = PointAmount.paid(amount);
        PointTransaction charged = PointTransaction.charged(memberId, command.idempotencyKey(), chargeAmount);
        charged = pointTransactionRepository.saveAndFlush(charged);

        balance.charge(amount);
        applicationEventPublisher.publishEvent(PointChargedTxEvent.from(charged));

        return PointBalanceInfo.from(balance);
    }

    private PointBalance findPointBalance(UUID memberId) {
        return pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
    }
}
