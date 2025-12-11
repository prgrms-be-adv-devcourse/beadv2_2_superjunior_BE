package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.MemberPointHistory;

import java.util.UUID;

interface MemberPointHistoryJpaRepository extends JpaRepository<MemberPointHistory, UUID> {
}
