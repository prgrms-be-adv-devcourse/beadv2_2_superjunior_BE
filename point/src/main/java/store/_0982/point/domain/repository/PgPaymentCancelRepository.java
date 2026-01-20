package store._0982.point.domain.repository;

import store._0982.point.domain.entity.PgPaymentCancel;

public interface PgPaymentCancelRepository {
    PgPaymentCancel save(PgPaymentCancel pgPaymentCancel);

    boolean existsByTransactionKey(String transactionKey);
}
