package store._0982.point.application.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.point.application.bonus.BonusEarningService;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.support.BaseKafkaTest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class OrderConfirmedEventListenerTest extends BaseKafkaTest {

    @MockitoBean
    private BonusEarningService bonusEarningService;

    @Test
    @DisplayName("주문 확정 이벤트를 수신하면 포인트 적립 로직이 실행된다")
    void handleOrderConfirmedEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();

        OrderConfirmedEvent event = new OrderConfirmedEvent(
                orderId,
                memberId,
                groupPurchaseId,
                "상품명",
                OrderConfirmedEvent.ProductCategory.FOOD
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CONFIRMED, event);

        // then
        awaitUntilAsserted(() ->
                verify(bonusEarningService, timeout(5000).times(1))
                        .processBonus(eq(memberId), any(BonusEarnCommand.class))
        );
    }
}
