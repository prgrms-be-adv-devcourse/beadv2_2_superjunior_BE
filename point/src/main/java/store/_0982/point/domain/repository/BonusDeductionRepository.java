package store._0982.point.domain.repository;

import store._0982.point.domain.entity.BonusDeduction;

import java.util.List;
import java.util.UUID;

public interface BonusDeductionRepository {

    BonusDeduction save(BonusDeduction bonusDeduction);

    BonusDeduction saveAndFlush(BonusDeduction bonusDeduction);

    void saveAll(Iterable<BonusDeduction> deductions);

    List<BonusDeduction> findByTransactionId(UUID transactionId);
}
