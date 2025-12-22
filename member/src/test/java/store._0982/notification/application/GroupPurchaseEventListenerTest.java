package store._0982.notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.notification.domain.notification.Notification;
import store._0982.notification.domain.notification.NotificationRepository;
import store._0982.notification.domain.notification.NotificationStatus;
import store._0982.notification.domain.notification.NotificationType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED}
)
class GroupPurchaseEventListenerTest {
    @Autowired
    private KafkaTemplate<String, GroupPurchaseChangedEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("공동 구매 성사 이벤트를 수신하면 공동 구매 완료 알림이 생성된다")
    void handleGroupPurchaseSuccessEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        GroupPurchaseChangedEvent event = new GroupPurchaseChangedEvent(
                UUID.randomUUID(),
                sellerId,
                "테스트 공동구매",
                GroupPurchaseChangedEvent.Status.SUCCESS,
                100000L
        );

        // when
        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.GROUP_PURCHASE_COMPLETED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("공동 구매 성사 완료");
            assertThat(notification.getMessage())
                    .contains("100,000원")
                    .contains("정산금에 추가될 예정");
        });
    }

    @Test
    @DisplayName("공동 구매 실패 이벤트를 수신하면 공동 구매 실패 알림이 생성된다")
    void handleGroupPurchaseFailedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        GroupPurchaseChangedEvent event = new GroupPurchaseChangedEvent(
                UUID.randomUUID(),
                sellerId,
                "실패 공동구매",
                GroupPurchaseChangedEvent.Status.FAILED,
                50000L
        );

        // when
        kafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.GROUP_PURCHASE_FAILED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("공동 구매 성사 실패");
            assertThat(notification.getMessage()).contains("성사 조건을 만족하지 못해");
        });
    }
}
