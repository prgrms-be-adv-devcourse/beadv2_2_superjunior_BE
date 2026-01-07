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
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.event.PointRechargedEvent;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentPointService {
    private final TossPaymentService tossPaymentService;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @ServiceLog
    @Transactional
    public PaymentPointCreateInfo createPaymentPoint(PaymentPointCommand command, UUID memberId) {
        UUID orderId = command.orderId();
        return paymentPointRepository.findByOrderId(orderId)
                .map(PaymentPointCreateInfo::from)
                .orElseGet(() -> {
                    try {
                        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, command.amount());
                        return PaymentPointCreateInfo.from(paymentPointRepository.saveAndFlush(paymentPoint));
                    } catch (DataIntegrityViolationException e) {
                        return paymentPointRepository.findByOrderId(orderId)
                                .map(PaymentPointCreateInfo::from)
                                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_CREATION_FAILED));
                    }
                });

    }

    public PageResponse<PaymentPointInfo> getPaymentHistories(UUID memberId, Pageable pageable) {
        Page<PaymentPointInfo> page = paymentPointRepository.findAllByMemberId(memberId, pageable)
                .map(PaymentPointInfo::from);
        return PageResponse.from(page);
    }

    @ServiceLog
    @Transactional
    public PaymentPointInfo confirmPayment(PointChargeConfirmCommand command, UUID memberId) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        paymentPoint.validate(memberId);
        if (paymentPoint.getStatus() == PaymentPointStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }

        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(paymentPoint, command);
        try {
            paymentPoint.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());
            memberPoint.addPoints(paymentPoint.getAmount());
            applicationEventPublisher.publishEvent(PointRechargedEvent.from(paymentPoint));
        } catch (Exception e) {
            rollbackPayment(command, paymentPoint);
        }

        return PaymentPointInfo.from(paymentPoint);
    }

    public PaymentPointInfo getPaymentHistory(UUID id, UUID memberId) {
        PaymentPoint paymentPoint = paymentPointRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        paymentPoint.validate(memberId);
        return PaymentPointInfo.from(paymentPoint);
    }

    private void rollbackPayment(PointChargeConfirmCommand command, PaymentPoint paymentPoint) {
        try {
            tossPaymentService.cancelPayment(paymentPoint, new PointRefundCommand(command.orderId(), "System Error"));
        } catch (Exception refundEx) {
            log.error("[Service] Failed to rollback payment", refundEx);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
        throw new CustomException(CustomErrorCode.PAYMENT_PROCESS_FAILED_REFUNDED);
    }
}
