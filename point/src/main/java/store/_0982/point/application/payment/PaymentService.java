package store._0982.point.application.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentCreateCommand;
import store._0982.point.application.dto.PaymentCreateInfo;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @ServiceLog
    @Transactional
    public PaymentCreateInfo createPayment(PaymentCreateCommand command, UUID memberId) {
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

    public PaymentInfo getPaymentHistory(UUID id, UUID memberId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        payment.validateOwner(memberId);
        return PaymentInfo.from(payment);
    }
}
