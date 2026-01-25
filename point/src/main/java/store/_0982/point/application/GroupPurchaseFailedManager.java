package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.application.pg.PgCancelFacade;
import store._0982.point.application.point.PointReturnService;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GroupPurchaseFailedManager {

    private final PointReturnService pointReturnService;
    private final PgCancelFacade pgCancelFacade;
    private final PointTransactionRepository pointTransactionRepository;
    private final PgPaymentRepository pgPaymentRepository;

    @Transactional
    public void selectRefundLogic(UUID memberId, UUID idempotencyKey, UUID orderId, String cancelReason) {
        if (paidWithPoints(orderId)) {
            PointReturnCommand command = new PointReturnCommand(
                    idempotencyKey,
                    orderId,
                    cancelReason,
                    null
            );
            pointReturnService.returnPoints(memberId, command);
        } else if (paidWithPg(orderId)) {
            PgCancelCommand command = new PgCancelCommand(
                    orderId,
                    cancelReason,
                    null
            );
            pgCancelFacade.refundPayment(memberId, command);
        } else {
            throw new CustomException(CustomErrorCode.NO_PAYMENT_HISTORY);
        }
    }

    private boolean paidWithPoints(UUID orderId) {
        return pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED);
    }

    private boolean paidWithPg(UUID orderId) {
        return pgPaymentRepository.existsByOrderIdAndStatus(orderId, PgPaymentStatus.COMPLETED);
    }
}
