package store._0982.point.infrastructure.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.repository.PgPaymentCancelRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PgPaymentCancelRepositoryAdapter implements PgPaymentCancelRepository {

    private final PgPaymentCancelJpaRepository pgPaymentCancelJpaRepository;

    @Override
    public PgPaymentCancel save(PgPaymentCancel pgPaymentCancel) {
        return pgPaymentCancelJpaRepository.save(pgPaymentCancel);
    }

    @Override
    public Set<String> findExistingTransactionKeys(List<String> transactionKeys) {
        return new HashSet<>(pgPaymentCancelJpaRepository.findAllTransactionKeysByTransactionKeyIn(transactionKeys));
    }

    @Override
    public void saveAllAndFlush(Iterable<PgPaymentCancel> pgPaymentCancels) {
        pgPaymentCancelJpaRepository.saveAllAndFlush(pgPaymentCancels);
    }
}
