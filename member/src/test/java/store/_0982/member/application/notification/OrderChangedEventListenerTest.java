package store._0982.member.application.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.ORDER_CHANGED}
)
class OrderChangedEventListenerTest {
    @Autowired
    private KafkaTemplate<String, OrderChangedEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("주문 생성 이벤트를 수신하면 주문 예약 알림이 생성된다")
    void handleOrderCreatedEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                UUID.randomUUID(),
                memberId,
                OrderChangedEvent.Status.CREATED,
                "테스트 상품"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CREATED, memberId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(memberId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.ORDER_SCHEDULED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).contains("테스트 상품");
            assertThat(notification.getMessage()).contains("공동 구매 신청이 완료");
        });
    }

    @Test
    @DisplayName("주문 성공 이벤트를 수신하면 주문 완료 알림이 생성된다")
    void handleOrderSuccessEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                UUID.randomUUID(),
                memberId,
                OrderChangedEvent.Status.SUCCESS,
                "성공 상품"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, memberId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(memberId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.ORDER_COMPLETED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).contains("성공 상품");
            assertThat(notification.getMessage()).contains("공동 구매가 확정");
        });
    }

    @Test
    @DisplayName("주문 실패 이벤트를 수신하면 주문 실패 알림이 생성된다")
    void handleOrderFailedEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                UUID.randomUUID(),
                memberId,
                OrderChangedEvent.Status.FAILED,
                "실패 상품"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, memberId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(memberId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.ORDER_FAILED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).contains("실패 상품");
            assertThat(notification.getMessage()).contains("공동 구매가 취소");
        });
    }
}
