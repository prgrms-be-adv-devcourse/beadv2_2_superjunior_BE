package store._0982.point.application;

import lombok.RequiredArgsConstructor;
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
import store._0982.point.domain.entity.PaymentPointFailure;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.repository.PaymentPointFailureRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentPointService {
    private final TossPaymentService tossPaymentService;
    private final PointEventPublisher pointEventPublisher;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    // TODO: 같은 orderId로 주문 생성이 동시에 여러 번 요청되었을 때, 낙관적 락이나 비관적 락을 이용할 것인가?
    @ServiceLog
    @Transactional
    public PaymentPointCreateInfo createPaymentPoint(PaymentPointCommand command, UUID memberId) {
        return paymentPointRepository.findByOrderId(command.orderId())
                .map(PaymentPointCreateInfo::from)
                .orElseGet(() -> {
                    PaymentPoint paymentPoint = PaymentPoint.create(memberId, command.orderId(), command.amount());
                    return PaymentPointCreateInfo.from(paymentPointRepository.save(paymentPoint));
                });
    }

    public PageResponse<PaymentPointHistoryInfo> getPaymentHistories(UUID memberId, Pageable pageable) {
        Page<PaymentPointHistoryInfo> page = paymentPointRepository.findAllByMemberId(memberId, pageable)
                .map(PaymentPointHistoryInfo::from);
        return PageResponse.from(page);
    }

    /*
    TODO: 결제 승인 도중 에러가 발생하면, 그냥 에러를 반환해서 클라이언트가 실패 처리를 요청하게 할까,
          아니면 서버에서 바로 실패 처리를 진행할까?
     */
    @ServiceLog
    @Transactional
    public void confirmPayment(PointChargeConfirmCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        if (paymentPoint.getStatus() == PaymentPointStatus.COMPLETED) {
            throw new CustomException(CustomErrorCode.ALREADY_COMPLETED_PAYMENT);
        }

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(paymentPoint, command);
        paymentPoint.markConfirmed(tossPaymentResponse.method(), tossPaymentResponse.approvedAt(), tossPaymentResponse.paymentKey());

        UUID memberId = paymentPoint.getMemberId();
        // 처음 결제 승인 시 보유 포인트 0인 객체를 미리 생성
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId)));

        memberPoint.addPoints(paymentPoint.getAmount());
        pointEventPublisher.publishPointRechargedEvent(paymentPoint);
    }

    @ServiceLog
    @Transactional
    public void handlePaymentFailure(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        switch (paymentPoint.getStatus()) {
            case COMPLETED, REFUNDED -> throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
            case FAILED -> {
                return;
            }
            case REQUESTED -> paymentPoint.markFailed(command.errorMessage());
        }

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);
        paymentPointFailureRepository.save(failure);
    }

    public PaymentPointHistoryInfo getPaymentHistory(UUID id, UUID memberId) {
        PaymentPoint paymentPoint = paymentPointRepository.findById(id)
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));
        paymentPoint.validate(memberId);
        return PaymentPointHistoryInfo.from(paymentPoint);
    }
}
