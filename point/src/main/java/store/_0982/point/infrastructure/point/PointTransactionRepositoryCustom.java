package store._0982.point.infrastructure.point;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.constant.PointType;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

public interface PointTransactionRepositoryCustom {

    Page<PointTransaction> findAllByMemberIdAndType(UUID memberId, PointType type, Pageable pageable);
}
