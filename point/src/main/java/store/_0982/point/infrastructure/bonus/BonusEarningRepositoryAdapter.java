package store._0982.point.infrastructure.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.BonusEarningStatus;
import store._0982.point.domain.entity.BonusEarning;
import store._0982.point.domain.repository.BonusEarningRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BonusEarningRepositoryAdapter implements BonusEarningRepository {

    private final BonusEarningJpaRepository bonusEarningJpaRepository;

    @Override
    public BonusEarning save(BonusEarning bonusEarning) {
        return bonusEarningJpaRepository.save(bonusEarning);
    }

    @Override
    public BonusEarning saveAndFlush(BonusEarning bonusEarning) {
        return bonusEarningJpaRepository.saveAndFlush(bonusEarning);
    }

    @Override
    public List<BonusEarning> findByMemberIdAndStatusInOrderByExpiresAtAsc(UUID memberId, List<BonusEarningStatus> statuses) {
        return bonusEarningJpaRepository.findByMemberIdAndStatusInOrderByExpiresAtAsc(memberId, statuses);
    }

    @Override
    public Optional<BonusEarning> findById(UUID id) {
        return bonusEarningJpaRepository.findById(id);
    }
}
