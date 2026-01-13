package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.repository.PgPaymentCancelRepository;

@Repository
@RequiredArgsConstructor
public class PgPaymentCancelRepositoryAdapter implements PgPaymentCancelRepository {

    private final PgPaymentCancelJpaRepository pgPaymentCancelJpaRepository;

    @Override
    public PgPaymentCancel save(PgPaymentCancel pgPaymentCancel) {
        return pgPaymentCancelJpaRepository.save(pgPaymentCancel);
    }
}
