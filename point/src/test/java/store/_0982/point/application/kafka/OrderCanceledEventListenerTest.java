package store._0982.point.application.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.point.PointReturnService;
import store._0982.point.support.BaseKafkaTest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderCanceledEventListenerTest extends BaseKafkaTest {

    @MockitoBean
    private PointReturnService pointReturnService;

    @MockitoBean
    private PgCancelService pgCancelService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("PG 결제 취소 이벤트를 수신하면 PgCancelService가 호출된다")
    void handleOrderCanceledEvent_Pg() {
        // given
        OrderCanceledEvent event = new OrderCanceledEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                memberId,
                orderId,
                OrderCanceledEvent.PaymentMethod.PG,
                10000L,
                "단순 변심"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELED, event);

        // then
        awaitUntilAsserted(() -> {
            verify(pgCancelService).refundPaymentPoint(eq(memberId), any(PgCancelCommand.class));
            verify(pointReturnService, never()).returnPoints(any(), any());
        });
    }

    @Test
    @DisplayName("포인트 결제 취소 이벤트를 수신하면 PointReturnService가 호출된다")
    void handleOrderCanceledEvent_Point() {
        // given
        OrderCanceledEvent event = new OrderCanceledEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                memberId,
                orderId,
                OrderCanceledEvent.PaymentMethod.POINT,
                5000L,
                "품절 취소"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CANCELED, event);

        // then
        awaitUntilAsserted(() -> {
            verify(pointReturnService).returnPoints(eq(memberId), any(PointReturnCommand.class));
            verify(pgCancelService, never()).refundPaymentPoint(any(), any());
        });
    }

}
