package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.PaymentCancel;
import store._0982.point.domain.repository.PaymentCancelRepository;

@Repository
@RequiredArgsConstructor
public class PaymentCancelRepositoryAdapter implements PaymentCancelRepository {

    private final PaymentCancelJpaRepository paymentCancelJpaRepository;

    @Override
    public PaymentCancel save(PaymentCancel paymentCancel) {
        return paymentCancelJpaRepository.save(paymentCancel);
    }
}
