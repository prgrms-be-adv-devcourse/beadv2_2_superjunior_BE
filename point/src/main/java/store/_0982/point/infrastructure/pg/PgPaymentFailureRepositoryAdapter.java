package store._0982.point.infrastructure.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.repository.PgPaymentFailureRepository;

@RequiredArgsConstructor
@Repository
public class PgPaymentFailureRepositoryAdapter implements PgPaymentFailureRepository {
    private final PgPaymentFailureJpaRepository repository;

    @Override
    public PgPaymentFailure save(PgPaymentFailure failure) {
        return repository.save(failure);
    }

}
