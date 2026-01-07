package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentPointInfo;
import store._0982.point.application.dto.PointChargeFailCommand;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;
import store._0982.point.domain.repository.PaymentPointFailureRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentPointFailureService {

    private final PaymentPointRepository paymentPointRepository;
    private final PaymentPointFailureRepository paymentPointFailureRepository;

    @ServiceLog
    @Transactional
    public PaymentPointInfo handlePaymentFailure(PointChargeFailCommand command, UUID memberId) {
        PaymentPoint paymentPoint = paymentPointRepository.findByOrderIdWithLock(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        paymentPoint.validate(memberId);
        switch (paymentPoint.getStatus()) {
            case COMPLETED, REFUNDED -> throw new CustomException(CustomErrorCode.CANNOT_HANDLE_FAILURE);
            case FAILED -> {
                return PaymentPointInfo.from(paymentPoint);
            }
            case REQUESTED -> paymentPoint.markFailed(command.errorMessage());
        }

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);
        paymentPointFailureRepository.save(failure);
        return PaymentPointInfo.from(paymentPoint);
    }
}
