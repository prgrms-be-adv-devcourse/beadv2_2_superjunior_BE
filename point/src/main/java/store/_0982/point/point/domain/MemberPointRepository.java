package store._0982.point.point.domain;

import java.util.Optional;
import java.util.UUID;

public interface MemberPointRepository {
    Optional<MemberPoint> findById(UUID memberId);
}
