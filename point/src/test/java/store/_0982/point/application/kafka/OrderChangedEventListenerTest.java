package store._0982.point.application.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.point.application.GroupPurchaseFailedManager;
import store._0982.point.support.BaseKafkaTest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OrderChangedEventListenerTest extends BaseKafkaTest {

    @MockitoBean
    private GroupPurchaseFailedManager groupPurchaseFailedManager;

    @Test
    @DisplayName("공동구매 실패 이벤트를 수신하면 환불 로직이 실행된다")
    void handleOrderChangedEvent_GroupPurchaseFail() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                orderId,
                memberId,
                OrderChangedEvent.Status.GROUP_PURCHASE_FAIL,
                "상품명"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, event);

        // then
        awaitUntilAsserted(() ->
                verify(groupPurchaseFailedManager).selectRefundLogic(memberId, event.getEventId(), orderId)
        );
    }

    @Test
    @DisplayName("공동구매 실패가 아닌 이벤트는 무시된다")
    void handleOrderChangedEvent_OtherStatus() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                orderId,
                memberId,
                OrderChangedEvent.Status.PAYMENT_COMPLETED,
                "상품명"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, event);

        // then
        awaitUntilAsserted(() ->
                verify(groupPurchaseFailedManager, timeout(5000).times(0))
                        .selectRefundLogic(any(), any(), any())
        );
    }
}
