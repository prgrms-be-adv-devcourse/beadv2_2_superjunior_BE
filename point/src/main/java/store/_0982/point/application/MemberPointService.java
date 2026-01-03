package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPointService {

    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OrderServiceClient orderServiceClient;

    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return MemberPointInfo.from(memberPoint);
    }

    @ServiceLog
    @Transactional
    public MemberPointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                memberPoint -> {
                    if (memberPointHistoryRepository.existsByOrderIdAndStatus(command.orderId(), MemberPointHistoryStatus.USED)) {
                        return MemberPointInfo.from(memberPoint);
                    }

                    long amount = command.amount();
                    try {
                        MemberPointHistory history = memberPointHistoryRepository.saveAndFlush(MemberPointHistory.used(memberId, command));
                        memberPoint.deductPoints(amount);
                        applicationEventPublisher.publishEvent(PointDeductedEvent.from(history));
                        return MemberPointInfo.from(memberPoint);
                    } catch (DataIntegrityViolationException e) {
                        return MemberPointInfo.from(memberPoint);
                    }
                }
        );
    }

    @ServiceLog
    @Transactional
    public MemberPointInfo returnPoints(UUID memberId, PointReturnCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                memberPoint -> {
                    UUID orderId = command.orderId();
                    if (memberPointHistoryRepository.existsByOrderIdAndStatus(orderId, MemberPointHistoryStatus.RETURNED)) {
                        return MemberPointInfo.from(memberPoint);
                    }

                    long amount = command.amount();
                    OrderInfo orderInfo = orderServiceClient.getOrder(orderId, memberId);
                    orderInfo.validateReturnable(memberId, orderId, amount);

                    try {
                        MemberPointHistory history = memberPointHistoryRepository.saveAndFlush(MemberPointHistory.returned(memberId, command));
                        memberPoint.addPoints(amount);
                        applicationEventPublisher.publishEvent(PointReturnedEvent.from(history));
                        return MemberPointInfo.from(memberPoint);
                    } catch (DataIntegrityViolationException e) {
                        return MemberPointInfo.from(memberPoint);
                    }
                }
        );
    }

    private MemberPointInfo processPointOperation(
            UUID memberId,
            UUID idempotencyKey,
            Function<MemberPoint, MemberPointInfo> operation
    ) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        if (memberPointHistoryRepository.existsByIdempotencyKey(idempotencyKey)) {
            return MemberPointInfo.from(memberPoint);
        }
        return operation.apply(memberPoint);
    }
}
