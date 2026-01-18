package store._0982.point.infrastructure.bonus;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.BonusDeduction;

import java.util.List;
import java.util.UUID;

public interface BonusDeductionJpaRepository extends JpaRepository<BonusDeduction, UUID> {

    List<BonusDeduction> findByTransactionId(UUID transactionId);
}
