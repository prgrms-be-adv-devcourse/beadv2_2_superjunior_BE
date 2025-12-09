package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.point.application.dto.*;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.exception.CustomErrorCode;
import store._0982.point.common.exception.CustomException;
import store._0982.point.common.log.ServiceLog;
import store._0982.point.domain.*;
import store._0982.point.presentation.dto.PointMinusRequest;
import store._0982.point.presentation.dto.PointReturnRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class PaymentPointService {
    private static final int REFUND_PERIOD_DAYS = 7;

    private final TossPaymentService tossPaymentService;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    // TODO: 같은 orderId로 주문 생성이 동시에 여러 번 요청되었을 때, 낙관적 락이나 비관적 락을 이용할 것인가?
    @ServiceLog
    public PaymentPointCreateInfo createPaymentPoint(PaymentPointCommand command, UUID memberId) {
        return paymentPointRepository.findByOrderId(command.orderId())
                .map(PaymentPointCreateInfo::from)
                .orElseGet(() -> {
                    PaymentPoint paymentPoint = PaymentPoint.create(memberId, command.orderId(), command.amount());
                    return PaymentPointCreateInfo.from(paymentPointRepository.save(paymentPoint));
                });
    }

    public MemberPointInfo getPoints(UUID memberId) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseGet(() -> memberPointRepository.save(new MemberPoint(memberId)));
        return MemberPointInfo.from(memberPoint);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentPointHistoryInfo> getPaymentHistory(UUID memberId, Pageable pageable) {
        Page<PaymentPointHistoryInfo> page = paymentPointRepository.findAllByMemberId(memberId, pageable)
                .map(PaymentPointHistoryInfo::from);
        return PageResponse.from(page);
    }

    /*
    TODO: 결제 승인 도중 에러가 발생하면, 그냥 에러를 반환해서 클라이언트가 실패 처리를 요청하게 할까,
          아니면 서버에서 바로 실패 처리를 진행할까?
     */
    @ServiceLog
    public PointChargeConfirmInfo confirmPayment(PointChargeConfirmCommand command) {
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
        return new PointChargeConfirmInfo(
                PaymentPointInfo.from(paymentPoint), MemberPointInfo.from(memberPoint));
    }

    // TODO: 한 번만 진행되어야 하는 포인트 차감이 동시에 여러 번 일어나지 않도록 검증이 필요할 것 같다.
    @ServiceLog
    public MemberPointInfo deductPoints(UUID memberId, PointMinusRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.deductPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }

    @ServiceLog
    public PointChargeFailInfo handlePaymentFailure(PointChargeFailCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        switch (paymentPoint.getStatus()) {
            case COMPLETED, REFUNDED -> throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
            case FAILED -> {
                PaymentPointFailure existingFailure = paymentPointFailureRepository.findByPaymentPoint(paymentPoint)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR));
                return PointChargeFailInfo.from(existingFailure);
            }
            case REQUESTED -> paymentPoint.markFailed(command.errorMessage());
        }

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);
        return PointChargeFailInfo.from(paymentPointFailureRepository.save(failure));
    }

    @ServiceLog
    public PointRefundInfo refundPaymentPoint(UUID memberId, PointRefundCommand command) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        if (!paymentPoint.getMemberId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH);
        }

        switch (paymentPoint.getStatus()) {
            case COMPLETED -> {
                // 정상 상태
            }
            case REQUESTED, FAILED -> throw new CustomException(CustomErrorCode.NOT_COMPLETED_PAYMENT);
            case REFUNDED -> {
                return PointRefundInfo.from(paymentPoint);
            }
        }

        validateRefundTerms(paymentPoint);
        MemberPoint memberPoint = memberPointRepository.findById(paymentPoint.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        TossPaymentResponse response = tossPaymentService.cancelPayment(paymentPoint, command);
        TossPaymentResponse.CancelInfo cancelInfo = response.cancels().get(0);

        paymentPoint.markRefunded(cancelInfo.canceledAt(), cancelInfo.cancelReason());
        memberPoint.refund(paymentPoint.getAmount());
        return PointRefundInfo.from(paymentPoint);
    }

    // TODO: 포인트 차감 / 반환 시 정상적인 요청이 맞는지 검증이 필요할 것 같다.
    @ServiceLog
    public MemberPointInfo returnPoints(UUID memberId, PointReturnRequest request) {
        MemberPoint memberPoint = memberPointRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        memberPoint.addPoints(request.amount());
        return MemberPointInfo.from(memberPoint);
    }

    private static void validateRefundTerms(PaymentPoint paymentPoint) {
        OffsetDateTime paymentAt = paymentPoint.getApprovedAt();
        if (paymentAt == null) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }

        // 결제일이 7일 이내일 경우 환불 가능
        if (Duration.between(paymentAt, OffsetDateTime.now()).toDays() > REFUND_PERIOD_DAYS) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }
    }
}
