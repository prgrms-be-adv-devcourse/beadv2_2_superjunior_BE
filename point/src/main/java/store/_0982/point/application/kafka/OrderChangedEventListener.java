package store._0982.point.application.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.point.application.GroupPurchaseFailedManager;
import store._0982.point.common.KafkaGroupIds;

@Component
@RequiredArgsConstructor
public class OrderChangedEventListener {

    private final GroupPurchaseFailedManager groupPurchaseFailedManager;

    // TODO: 여기도 뭔가 구조가 잘못된 것 같다. 결제 수단 별로 토픽이 쪼개져야 성능 상 오버헤드가 적을 것 같다
    @RetryableTopic
    @KafkaListener(
            topics = KafkaTopics.ORDER_CHANGED,
            groupId = KafkaGroupIds.PAYMENT_SERVICE,
            containerFactory = "orderChangedEventListenerContainerFactory"
    )
    public void handleOrderChangedEvent(OrderChangedEvent event) {
        if (event.getStatus() != OrderChangedEvent.Status.GROUP_PURCHASE_FAIL) {
            return;
        }
        groupPurchaseFailedManager.selectRefundLogic(
                event.getMemberId(),
                event.getEventId(),
                event.getId(),
                "공동 구매 성사 실패"
        );
    }
}
