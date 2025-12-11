package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.domain.MemberPoint;
import store._0982.point.domain.MemberPointRepository;
import store._0982.point.exception.CustomErrorCode;
import store._0982.point.presentation.dto.PointMinusRequest;
import store._0982.point.presentation.dto.PointReturnRequest;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPointService {
    private final MemberPointRepository memberPointRepository;

    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId)));
        return MemberPointInfo.from(memberPoint);
    }

    // TODO: 한 번만 진행되어야 하는 포인트 차감이 동시에 여러 번 일어나지 않도록 검증이 필요할 것 같다.
    @ServiceLog
    @Transactional
    public MemberPointInfo deductPoints(UUID memberId, PointMinusRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.deductPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }

    // TODO: 포인트 차감 / 반환 시 정상적인 요청이 맞는지 검증이 필요할 것 같다.
    @ServiceLog
    @Transactional
    public MemberPointInfo returnPoints(UUID memberId, PointReturnRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.addPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }
}
