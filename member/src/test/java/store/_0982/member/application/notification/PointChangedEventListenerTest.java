package store._0982.member.application.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {"point.recharged", "point.changed"}
)
class PointChangedEventListenerTest {
    @Autowired
    private KafkaTemplate<String, PointChangedEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("포인트 충전 이벤트를 수신하면 알림이 생성된다")
    void handlePointRechargedEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        PointChangedEvent event = new PointChangedEvent(
                UUID.randomUUID(),
                memberId,
                1000L,
                PointChangedEvent.Status.CHARGED,
                "간편결제"
        );

        // when
        kafkaTemplate.send(KafkaTopics.POINT_CHANGED, memberId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(memberId, NotificationStatus.SENT);

            assertThat(notifications).hasSize(1);
            assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.POINT_RECHARGED);
            assertThat(notifications.get(0).getMessage()).contains("1,000원");
        });
    }
}
