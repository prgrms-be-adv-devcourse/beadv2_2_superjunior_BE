package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.event.PaymentConfirmedEvent;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.domain.repository.PointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentConfirmService {

    private final TossPaymentService tossPaymentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;

    @ServiceLog
    @Transactional
    public PaymentInfo confirmPayment(PaymentConfirmCommand command, UUID memberId) {
        Payment payment = paymentRepository.findByPgOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        payment.validate(memberId);
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }

        Point point = pointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(payment, command);
        try {
            payment.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());
            point.charge(payment.getAmount());
            applicationEventPublisher.publishEvent(PaymentConfirmedEvent.from(payment));
        } catch (Exception e) {
            rollbackPayment(command, payment);
        }

        return PaymentInfo.from(payment);
    }

    private void rollbackPayment(PaymentConfirmCommand command, Payment payment) {
        try {
            tossPaymentService.cancelPayment(payment, new PointRefundCommand(command.orderId(), "System Error"));
        } catch (Exception refundEx) {
            log.error("[Service] Failed to rollback payment", refundEx);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
        throw new CustomException(CustomErrorCode.PAYMENT_PROCESS_FAILED_REFUNDED);
    }
}
