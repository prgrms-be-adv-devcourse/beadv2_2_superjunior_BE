package store._0982.point.infrastructure.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.BonusDeduction;
import store._0982.point.domain.repository.BonusDeductionRepository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BonusDeductionRepositoryAdapter implements BonusDeductionRepository {

    private final BonusDeductionJpaRepository bonusDeductionJpaRepository;

    @Override
    public BonusDeduction save(BonusDeduction bonusDeduction) {
        return bonusDeductionJpaRepository.save(bonusDeduction);
    }

    @Override
    public BonusDeduction saveAndFlush(BonusDeduction bonusDeduction) {
        return bonusDeductionJpaRepository.saveAndFlush(bonusDeduction);
    }

    @Override
    public void saveAll(Iterable<BonusDeduction> deductions) {
        bonusDeductionJpaRepository.saveAll(deductions);
    }

    @Override
    public List<BonusDeduction> findByTransactionId(UUID transactionId) {
        return bonusDeductionJpaRepository.findByTransactionId(transactionId);
    }
}
