package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.event.PaymentConfirmedEvent;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final TossPaymentService tossPaymentService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentRepository paymentRepository;

    @ServiceLog
    @Transactional
    public void confirmPayment(PaymentConfirmCommand command, UUID memberId) {
        Payment payment = paymentRepository.findByPaymentKey(command.paymentKey())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        payment.validateCompletable(memberId);

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(payment, command);
        try {
            payment.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());
            applicationEventPublisher.publishEvent(PaymentConfirmedEvent.from(payment));
        } catch (Exception e) {
            rollbackPayment(command, payment);
        }
    }

    // 1차 방어선: 결제 성공 처리에 실패했을 때 토스 API에 취소 요청
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
