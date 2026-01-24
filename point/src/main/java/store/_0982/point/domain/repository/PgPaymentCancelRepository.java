package store._0982.point.domain.repository;

import store._0982.point.domain.entity.PgPaymentCancel;

import java.util.List;
import java.util.Set;

public interface PgPaymentCancelRepository {
    PgPaymentCancel save(PgPaymentCancel pgPaymentCancel);

    Set<String> findExistingTransactionKeys(List<String> transactionKeys);

    void saveAllAndFlush(Iterable<PgPaymentCancel> pgPaymentCancels);
}
