package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.MemberPointHistory;
import store._0982.point.domain.repository.MemberPointHistoryRepository;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.constant.MemberPointHistoryStatus;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberPointService {
    private final MemberPointRepository memberPointRepository;
    private final MemberPointHistoryRepository memberPointHistoryRepository;
    private final PointEventPublisher pointEventPublisher;

    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId)));
        return MemberPointInfo.from(memberPoint);
    }

    @ServiceLog
    public MemberPointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        UUID idempotencyKey = command.idempotencyKey();
        if (memberPointHistoryRepository.existsByIdempotencyKeyAndStatus(
                idempotencyKey, MemberPointHistoryStatus.USED)) {
            return MemberPointInfo.from(memberPoint);
        }

        int amount = command.amount();
        memberPoint.deductPoints(amount);
        MemberPointHistory history = memberPointHistoryRepository.save(MemberPointHistory.used(memberId, command));
        pointEventPublisher.publishPointDeductedEvent(history);
        return MemberPointInfo.from(memberPoint);
    }

    /*
    TODO: 포인트 차감 / 반환 시 정상적인 요청이 맞는지 검증이 필요할 것 같다.
        멱등키 검증은 적용했지만, 추가적인 검증이 있어야 한다.
        주문 ID 등을 추가로 요청받아서 주문 정보 조회 API 호출
        -> 주문한 사용자와 일치하는지, 이미 환불된 요청은 아닌지 등의 검증 필요
     */
    @ServiceLog
    public MemberPointInfo returnPoints(UUID memberId, PointReturnCommand command) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        UUID idempotencyKey = command.idempotencyKey();
        if (memberPointHistoryRepository.existsByIdempotencyKeyAndStatus(
                idempotencyKey, MemberPointHistoryStatus.RETURNED)) {
            return MemberPointInfo.from(memberPoint);
        }

        int amount = command.amount();
        memberPoint.addPoints(amount);
        MemberPointHistory history = memberPointHistoryRepository.save(MemberPointHistory.returned(memberId, command));
        pointEventPublisher.publishPointReturnedEvent(history);
        return MemberPointInfo.from(memberPoint);
    }
}
