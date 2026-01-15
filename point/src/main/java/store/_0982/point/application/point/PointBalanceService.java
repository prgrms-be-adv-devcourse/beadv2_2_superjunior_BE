package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointBalanceService {

    private final PointBalanceRepository pointBalanceRepository;

    public PointInfo getPoints(UUID memberId) {
        PointBalance pointBalance = pointBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));
        return PointInfo.from(pointBalance);
    }
}
