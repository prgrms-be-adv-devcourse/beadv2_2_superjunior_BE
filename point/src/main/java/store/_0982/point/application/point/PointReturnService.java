package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointPayment;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointReturnService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PointBalanceRepository pointBalanceRepository;
    private final PointPaymentRepository pointPaymentRepository;
    private final OrderServiceClient orderServiceClient;

    @ServiceLog
    @Transactional
    public void returnPoints(UUID memberId, PointReturnCommand command) {
        UUID orderId = command.orderId();
        if (pointPaymentRepository.existsByOrderIdAndStatus(orderId, PointPaymentStatus.RETURNED)) {
            return;
        }

        long amount = command.amount();
        OrderInfo orderInfo = orderServiceClient.getOrder(orderId, memberId);
        orderInfo.validateReturnable(memberId, orderId, amount);

        PointBalance point = pointBalanceRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        PointPayment returned = PointPayment.returned(
                memberId, orderId, command.idempotencyKey(), amount, 0, command.cancelReason());
        PointPayment history = pointPaymentRepository.saveAndFlush(returned);
        point.charge(amount);
        applicationEventPublisher.publishEvent(PointReturnedEvent.from(history));
    }
}
