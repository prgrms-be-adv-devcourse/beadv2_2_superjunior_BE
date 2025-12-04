package store._0982.point.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.point.domain.MemberPoint;
import store._0982.point.point.domain.MemberPointRepository;

import java.util.UUID;

public interface MemberPointJpaRepository extends JpaRepository<MemberPoint,UUID> {
}
