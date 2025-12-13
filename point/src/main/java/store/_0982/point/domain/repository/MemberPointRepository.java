package store._0982.point.domain.repository;

import store._0982.point.domain.entity.MemberPoint;

import java.util.Optional;
import java.util.UUID;

public interface MemberPointRepository {
    Optional<MemberPoint> findById(UUID memberId);

    MemberPoint save(MemberPoint afterPayment);
}
