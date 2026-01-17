package store._0982.point.infrastructure.bonus;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.entity.BonusEarning;

import java.util.List;
import java.util.UUID;

public interface BonusEarningJpaRepository extends JpaRepository<BonusEarning, UUID> {

    List<BonusEarning> findByMemberIdAndStatusInOrderByExpiresAtAsc(UUID memberId, List<BonusEarningStatus> statuses);
}
