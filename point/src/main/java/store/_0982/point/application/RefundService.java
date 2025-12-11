package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.application.dto.PointRefundInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {
    private static final int REFUND_PERIOD_DAYS = 7;

    private final TossPaymentService tossPaymentService;
    private final PaymentPointRepository paymentPointRepository;
    private final MemberPointRepository memberPointRepository;

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
