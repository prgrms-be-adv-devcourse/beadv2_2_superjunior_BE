package store._0982.point.domain.repository;

import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;

import java.util.Optional;

public interface PaymentPointFailureRepository {
    PaymentPointFailure save(PaymentPointFailure failure);

    Optional<PaymentPointFailure> findByPaymentPoint(PaymentPoint paymentPoint);
}
