package store._0982.point.infrastructure.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;

import java.util.List;
import java.util.UUID;

public interface PgPaymentCancelJpaRepository extends JpaRepository<PgPaymentCancel, UUID> {

    List<PgPaymentCancel> findAllByPgPayment(PgPayment pgPayment);

    @Query("SELECT pc.transactionKey FROM PgPaymentCancel pc WHERE pc.transactionKey IN :transactionKeys")
    List<String> findAllTransactionKeysByTransactionKeyIn(List<String> transactionKeys);
}
