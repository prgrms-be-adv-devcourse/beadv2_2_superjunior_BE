package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgQueryService {

    private final PgPaymentRepository pgPaymentRepository;
    private final PaymentRules paymentRules;

    public PgPayment findCompletablePayment(UUID orderId, UUID memberId) {
        PgPayment pgPayment = findPayment(orderId);
        pgPayment.validateCompletable(memberId);
        return pgPayment;
    }

    public PgPayment findFailablePayment(UUID orderId, UUID memberId) {
        PgPayment pgPayment = findPayment(orderId);
        pgPayment.validateFailable(memberId);
        return pgPayment;
    }

    public PgPayment findRefundablePayment(UUID orderId, UUID memberId) {
        PgPayment pgPayment = findPayment(orderId);
        pgPayment.validateRefundable(memberId, paymentRules);
        return pgPayment;
    }

    private PgPayment findPayment(UUID orderId) {
        return pgPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
    }
}
