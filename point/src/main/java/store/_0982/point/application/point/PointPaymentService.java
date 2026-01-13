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
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointPayment;
import store._0982.point.domain.event.PointChargedEvent;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.repository.PointPaymentRepository;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointPaymentService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointPaymentRepository pointPaymentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderServiceClient orderServiceClient;

    public PointInfo getPoints(UUID memberId) {
        PointBalance pointBalance = pointBalanceRepository.findById(memberId)
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
                    PointPayment charged = PointPayment.charged(memberId, idempotencyKey, amount, 0);

                    PointPayment history = pointPaymentRepository.saveAndFlush(charged);
                    point.charge(amount);
                    applicationEventPublisher.publishEvent(PointChargedEvent.from(history));
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
                    if (pointPaymentRepository.existsByOrderIdAndStatus(orderId, PointPaymentStatus.USED)) {
                        return PointInfo.from(point);
                    }

                    long amount = command.amount();
                    OrderInfo orderInfo = orderServiceClient.getOrder(orderId, memberId);
                    orderInfo.validateDeductible(memberId, orderId, amount);

                    return executeIdempotentAction(point, () -> {
                        PointPayment used = PointPayment.used(memberId, orderId, idempotencyKey, amount, 0);
                        PointPayment history = pointPaymentRepository.saveAndFlush(used);
                        point.use(amount);
                        applicationEventPublisher.publishEvent(PointDeductedEvent.from(history));
                    });
                }
        );
    }

    private PointInfo processPointOperation(
            UUID memberId,
            UUID idempotencyKey,
            Function<PointBalance, PointInfo> operation
    ) {
        PointBalance pointBalance = pointBalanceRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        if (pointPaymentRepository.existsByIdempotencyKey(idempotencyKey)) {
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
