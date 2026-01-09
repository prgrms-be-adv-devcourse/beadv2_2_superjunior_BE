package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.PointHistoryStatus;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.entity.PointHistory;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.PointHistoryRepository;
import store._0982.point.domain.repository.PointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderServiceClient orderServiceClient;

    public PointInfo getPoints(UUID memberId) {
        Point point = pointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return PointInfo.from(point);
    }

    @ServiceLog
    @Transactional
    public PointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                point -> {
                    if (pointHistoryRepository.existsByOrderIdAndStatus(command.orderId(), PointHistoryStatus.USED)) {
                        return PointInfo.from(point);
                    }

                    long amount = command.amount();
                    try {
                        PointHistory history = pointHistoryRepository.saveAndFlush(PointHistory.used(memberId, command));
                        point.use(amount);
                        applicationEventPublisher.publishEvent(PointDeductedEvent.from(history));
                        return PointInfo.from(point);
                    } catch (DataIntegrityViolationException e) {
                        return PointInfo.from(point);
                    }
                }
        );
    }

    @ServiceLog
    @Transactional
    public PointInfo returnPoints(UUID memberId, PointReturnCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                point -> {
                    UUID orderId = command.orderId();
                    if (pointHistoryRepository.existsByOrderIdAndStatus(orderId, PointHistoryStatus.RETURNED)) {
                        return PointInfo.from(point);
                    }

                    long amount = command.amount();
                    OrderInfo orderInfo = orderServiceClient.getOrder(orderId, memberId);
                    orderInfo.validateReturnable(memberId, orderId, amount);

                    try {
                        PointHistory history = pointHistoryRepository.saveAndFlush(PointHistory.returned(memberId, command));
                        point.recharge(amount);
                        applicationEventPublisher.publishEvent(PointReturnedEvent.from(history));
                        return PointInfo.from(point);
                    } catch (DataIntegrityViolationException e) {
                        return PointInfo.from(point);
                    }
                }
        );
    }

    private PointInfo processPointOperation(
            UUID memberId,
            UUID idempotencyKey,
            Function<Point, PointInfo> operation
    ) {
        Point point = pointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        if (pointHistoryRepository.existsByIdempotencyKey(idempotencyKey)) {
            return PointInfo.from(point);
        }
        return operation.apply(point);
    }
}
