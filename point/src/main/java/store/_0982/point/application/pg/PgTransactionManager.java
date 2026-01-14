package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PgFailCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.common.RetryForTransactional;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.entity.PgPaymentFailure;
import store._0982.point.domain.event.PaymentConfirmedEvent;
import store._0982.point.domain.repository.PgPaymentCancelRepository;
import store._0982.point.domain.repository.PgPaymentFailureRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PgTransactionManager {

    private final PgPaymentRepository pgPaymentRepository;
    private final PgPaymentCancelRepository pgPaymentCancelRepository;
    private final PgPaymentFailureRepository pgPaymentFailureRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PgPayment findPayment(String paymentKey) {
        return pgPaymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
    }

    public PgPayment findCompletablePayment(String paymentKey, UUID memberId) {
        PgPayment pgPayment = findPayment(paymentKey);
        pgPayment.validateCompletable(memberId);
        return pgPayment;
    }

    public PgPayment findFailablePayment(String paymentKey, UUID memberId) {
        PgPayment pgPayment = findPayment(paymentKey);
        pgPayment.validateFailable(memberId);
        return pgPayment;
    }

    public PgPayment markRefundPending(UUID orderId, UUID memberId) {
        PgPayment pgPayment = pgPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        pgPayment.validateRefundable(memberId);
        pgPayment.markRefundPending();
        return pgPayment;
    }

    @Transactional
    @RetryForTransactional
    public void markConfirmedPayment(TossPaymentResponse tossPaymentResponse, String paymentKey, UUID memberId) {
        PgPayment pgPayment = findCompletablePayment(paymentKey, memberId);
        pgPayment.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());
        applicationEventPublisher.publishEvent(PaymentConfirmedEvent.from(pgPayment));
    }

    @Transactional
    @RetryForTransactional
    public void markFailedPaymentBySystem(String errorMessage, String paymentKey, UUID memberId) {
        PgPayment pgPayment = findFailablePayment(paymentKey, memberId);
        pgPayment.markFailed(errorMessage);
        PgPaymentFailure pgPaymentFailure = PgPaymentFailure.systemError(pgPayment);
        pgPaymentFailureRepository.save(pgPaymentFailure);
    }

    @Transactional
    @RetryForTransactional
    public void markFailedPaymentByPg(PgFailCommand command, UUID memberId) {
        PgPayment pgPayment = findFailablePayment(command.paymentKey(), memberId);
        pgPayment.markFailed(command.errorMessage());
        PgPaymentFailure pgPaymentFailure = PgPaymentFailure.pgError(pgPayment, command);
        pgPaymentFailureRepository.save(pgPaymentFailure);
    }

    @Transactional
    @RetryForTransactional
    public void markRefundedPayment(TossPaymentResponse tossPaymentResponse, UUID orderId, UUID memberId) {
        PgPayment pgPayment = markRefundPending(orderId, memberId);
        TossPaymentResponse.CancelInfo cancelInfo = tossPaymentResponse.cancels().get(0);
        pgPayment.markRefunded(cancelInfo.canceledAt(), cancelInfo.cancelReason());

        PgPaymentCancel pgPaymentCancel = PgPaymentCancel.from(pgPayment, cancelInfo.cancelReason(), cancelInfo.cancelAmount(), cancelInfo.canceledAt());
        pgPaymentCancelRepository.save(pgPaymentCancel);
    }
}