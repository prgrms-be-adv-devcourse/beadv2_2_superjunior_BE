package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.event.PointDeductedEvent;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberPointService {
    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId)));
        return MemberPointInfo.from(memberPoint);
    }

    @ServiceLog
    public MemberPointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                memberPoint -> {
                    long amount = command.amount();
                    memberPoint.deductPoints(amount);
                    MemberPointHistory history = memberPointHistoryRepository.save(MemberPointHistory.used(memberId, command));
                    applicationEventPublisher.publishEvent(PointDeductedEvent.from(history));
                    return MemberPointInfo.from(memberPoint);
                }
        );
    }

    @ServiceLog
    public MemberPointInfo returnPoints(UUID memberId, PointReturnCommand command) {
        return processPointOperation(
                memberId,
                command.idempotencyKey(),
                memberPoint -> {
                    long amount = command.amount();
                    memberPoint.addPoints(amount);
                    MemberPointHistory history = memberPointHistoryRepository.save(MemberPointHistory.returned(memberId, command));
                    applicationEventPublisher.publishEvent(PointReturnedEvent.from(history));
                    return MemberPointInfo.from(memberPoint);
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
