package store._0982.point.infrastructure.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;

import java.util.List;
import java.util.UUID;

public interface PgPaymentCancelJpaRepository extends JpaRepository<PgPaymentCancel, UUID> {

    List<PgPaymentCancel> findAllByPgPayment(PgPayment pgPayment);
}
