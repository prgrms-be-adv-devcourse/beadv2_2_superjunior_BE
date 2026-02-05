package store._0982.point.domain.repository;

import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.entity.BonusEarning;

import java.util.List;
import java.util.UUID;

public interface BonusEarningRepository {

    BonusEarning save(BonusEarning bonusEarning);

    BonusEarning saveAndFlush(BonusEarning bonusEarning);

    List<BonusEarning> findByMemberIdAndStatusInOrderByExpiresAtAsc(UUID memberId, List<BonusEarningStatus> statuses);

    List<BonusEarning> findAllById(Iterable<UUID> ids);

    void saveAll(Iterable<BonusEarning> earnings);
}
