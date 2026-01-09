package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.*;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.event.PointRechargedEvent;
import store._0982.point.domain.repository.PointRepository;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final TossPaymentService tossPaymentService;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @ServiceLog
    @Transactional
    public PaymentCreateInfo createPaymentPoint(PaymentCreateCommand command, UUID memberId) {
        UUID orderId = command.orderId();
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentCreateInfo::from)
                .orElseGet(() -> {
                    try {
                        Payment payment = Payment.create(memberId, orderId, command.amount());
                        return PaymentCreateInfo.from(paymentRepository.saveAndFlush(payment));
                    } catch (DataIntegrityViolationException e) {
                        return paymentRepository.findByOrderId(orderId)
                                .map(PaymentCreateInfo::from)
                                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_CREATION_FAILED));
                    }
                });

    }

    public PageResponse<PaymentInfo> getPaymentHistories(UUID memberId, Pageable pageable) {
        Page<PaymentInfo> page = paymentRepository.findAllByMemberId(memberId, pageable)
                .map(PaymentInfo::from);
        return PageResponse.from(page);
    }

    @ServiceLog
    @Transactional
    public PaymentInfo confirmPayment(PaymentConfirmCommand command, UUID memberId) {
        Payment payment = paymentRepository.findByOrderId(command.orderId())
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
            point.recharge(payment.getAmount());
            applicationEventPublisher.publishEvent(PointRechargedEvent.from(payment));
        } catch (Exception e) {
            rollbackPayment(command, payment);
        }

        return PaymentInfo.from(payment);
    }

    public PaymentInfo getPaymentHistory(UUID id, UUID memberId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        payment.validate(memberId);
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
