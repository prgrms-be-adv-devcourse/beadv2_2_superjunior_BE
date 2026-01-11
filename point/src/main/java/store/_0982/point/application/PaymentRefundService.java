package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.application.dto.PointRefundInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.repository.PointRepository;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.repository.PaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {
    private static final int REFUND_PERIOD_DAYS = 7;

    private final TossPaymentService tossPaymentService;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;

    @ServiceLog
    @Transactional
    public PointRefundInfo refundPaymentPoint(UUID memberId, PointRefundCommand command) {
        Payment payment = paymentRepository.findByOrderId(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        payment.validateOwner(memberId);
        switch (payment.getStatus()) {
            case COMPLETED -> {
                // 정상 상태
            }
            case PENDING, FAILED -> throw new CustomException(CustomErrorCode.NOT_COMPLETED_PAYMENT);
            case REFUNDED -> {
                return PointRefundInfo.from(payment);
            }
        }

        validateRefundTerms(payment);
        Point point = pointRepository.findById(payment.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        TossPaymentResponse response = tossPaymentService.cancelPayment(payment, command);
        TossPaymentResponse.CancelInfo cancelInfo = response.cancels().get(0);

        payment.markRefunded(cancelInfo.canceledAt(), cancelInfo.cancelReason());
        point.transfer(payment.getAmount());
        return PointRefundInfo.from(payment);
    }

    private static void validateRefundTerms(Payment payment) {
        OffsetDateTime paymentAt = payment.getApprovedAt();
        if (paymentAt == null) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }

        // 결제일이 7일 이내일 경우 환불 가능
        if (Duration.between(paymentAt, OffsetDateTime.now()).toDays() > REFUND_PERIOD_DAYS) {
            throw new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED);
        }
    }
}
