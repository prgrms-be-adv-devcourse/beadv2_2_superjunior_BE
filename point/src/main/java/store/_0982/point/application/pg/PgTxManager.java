package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.RetryableTransactional;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;
import store._0982.point.domain.repository.PgPaymentCancelRepository;
import store._0982.point.domain.repository.PgPaymentFailureRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgTxManager {

    private final PgPaymentRepository pgPaymentRepository;
    private final PgPaymentCancelRepository pgPaymentCancelRepository;
    private final PgPaymentFailureRepository pgPaymentFailureRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
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

    @RetryableTransactional
    public void markConfirmedPayment(TossPaymentInfo tossPaymentInfo, UUID orderId, UUID memberId) {
        PgPayment pgPayment = findCompletablePayment(orderId, memberId);
        pgPayment.markConfirmed(tossPaymentInfo.paymentMethod(), tossPaymentInfo.approvedAt(), tossPaymentInfo.paymentKey());
        applicationEventPublisher.publishEvent(PaymentConfirmedTxEvent.from(pgPayment));
    }

    @RetryableTransactional
    public void markFailedPaymentBySystem(String errorMessage, String paymentKey, UUID orderId, UUID memberId) {
        PgPayment pgPayment = findFailablePayment(orderId, memberId);
        pgPayment.markFailed(paymentKey);
        PgPaymentFailure pgPaymentFailure = PgPaymentFailure.systemError(pgPayment, errorMessage);
        pgPaymentFailureRepository.save(pgPaymentFailure);
    }

    @RetryableTransactional
    public void markRefundedPayment(TossPaymentInfo tossPaymentInfo, UUID orderId, UUID memberId) {
        PgPayment pgPayment = findRefundablePayment(orderId, memberId);

        List<String> incomingKeys = tossPaymentInfo.cancels().stream()
                .map(TossPaymentInfo.CancelInfo::transactionKey)
                .toList();
        Set<String> existingKeys = pgPaymentCancelRepository.findExistingTransactionKeys(incomingKeys);

        List<PgPaymentCancel> newCancels = new ArrayList<>();
        for (TossPaymentInfo.CancelInfo cancelInfo : tossPaymentInfo.cancels()) {
            if (!existingKeys.contains(cancelInfo.transactionKey())) {
                PgPaymentCancel pgPaymentCancel = PgPaymentCancel.from(
                        pgPayment,
                        cancelInfo.cancelReason(),
                        cancelInfo.cancelAmount(),
                        cancelInfo.canceledAt(),
                        cancelInfo.transactionKey()
                );
                newCancels.add(pgPaymentCancel);
                pgPayment.applyRefund(cancelInfo.cancelAmount(), cancelInfo.canceledAt());
            }
        }

        if (!newCancels.isEmpty()) {
            pgPaymentCancelRepository.saveAll(newCancels);
        }
    }

    private PgPayment findPayment(UUID orderId) {
        return pgPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
    }
}
