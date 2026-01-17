package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.OrderValidator;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.common.RetryForTransactional;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.BonusEarningRepository;
import store._0982.point.domain.repository.BonusPolicyRepository;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusEarningService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final BonusPolicyRepository bonusPolicyRepository;
    private final BonusEarningRepository bonusEarningRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final OrderValidator orderValidator;

    @Transactional
    @RetryForTransactional
    public void earnBonusPoint(UUID memberId, BonusEarnCommand command) {
        PointTransaction transaction = pointTransactionRepository
                .findByOrderIdAndStatus(command.orderId(), PointTransactionStatus.USED)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        transaction.validateOwner(memberId);

        // TODO(human)
    }
}
