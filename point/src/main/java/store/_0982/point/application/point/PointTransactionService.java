package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointChargeCommand;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.application.OrderValidator;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointChargedEvent;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointTransactionService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderValidator orderValidator;

    public PointInfo getPoints(UUID memberId) {
        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return PointInfo.from(pointBalance);
    }

    // TODO: 계좌이체 API를 넣을까? 고도화를 어떻게 할지도 생각하자. 우선은 중복 요청 처리는 하지 말자
    @ServiceLog
    @Transactional
    public PointInfo chargePoints(PointChargeCommand command, UUID memberId) {
        UUID idempotencyKey = command.idempotencyKey();
        return processPointOperation(
                memberId,
                idempotencyKey,
                point -> executeIdempotentAction(point, () -> {
                    long amount = command.amount();
                    PointTransaction charged = PointTransaction.charged(memberId, idempotencyKey, PointAmount.of(amount, 0));
                    charged = pointTransactionRepository.saveAndFlush(charged);
                    point.charge(amount);
                    applicationEventPublisher.publishEvent(PointChargedEvent.from(charged));
                })
        );
    }

    @ServiceLog
    @Transactional
    public PointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        UUID idempotencyKey = command.idempotencyKey();
        return processPointOperation(
                memberId,
                idempotencyKey,
                point -> {
                    UUID orderId = command.orderId();
                    if (pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED)) {
                        return PointInfo.from(point);
                    }

                    long amount = command.amount();
                    orderValidator.validateOrderDeductible(memberId, orderId, amount);

                    return executeIdempotentAction(point, () -> {
                        PointAmount deduction = point.use(amount);
                        PointTransaction used = PointTransaction.used(memberId, orderId, idempotencyKey, deduction);
                        used = pointTransactionRepository.saveAndFlush(used);
                        applicationEventPublisher.publishEvent(PointDeductedEvent.from(used));
                    });
                }
        );
    }

    private PointInfo processPointOperation(
            UUID memberId,
            UUID idempotencyKey,
            Function<PointBalance, PointInfo> operation
    ) {
        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        if (pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            return PointInfo.from(pointBalance);
        }
        return operation.apply(pointBalance);
    }

    private PointInfo executeIdempotentAction(PointBalance pointBalance, Runnable action) {
        try {
            action.run();
            return PointInfo.from(pointBalance);
        } catch (DuplicateKeyException e) {
            return PointInfo.from(pointBalance);
        }
    }
}
