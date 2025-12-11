package store._0982.notification.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementEvent;
import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationRepository;
import store._0982.notification.domain.NotificationStatus;
import store._0982.notification.domain.NotificationType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {
                KafkaTopics.DAILY_SETTLEMENT_COMPLETED,
                KafkaTopics.DAILY_SETTLEMENT_FAILED,
                KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED,
                KafkaTopics.MONTHLY_SETTLEMENT_FAILED
        }
)
class SettlementEventListenerTest {
    @Autowired
    private KafkaTemplate<String, SettlementEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("일일 정산 완료 이벤트를 수신하면 일일 정산 완료 알림이 생성된다")
    void handleDailySettlementCompletedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        SettlementEvent event = new SettlementEvent(
                UUID.randomUUID(),
                sellerId,
                java.time.OffsetDateTime.now().minusDays(1),
                java.time.OffsetDateTime.now(),
                SettlementEvent.Status.SUCCESS,
                50000L,
                java.math.BigDecimal.valueOf(500),
                java.math.BigDecimal.valueOf(49500)
        );

        // when
        kafkaTemplate.send(KafkaTopics.DAILY_SETTLEMENT_COMPLETED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.DAILY_SETTLEMENT_COMPLETED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("일일 정산 완료");
            assertThat(notification.getMessage()).contains("49,500원");
            assertThat(notification.getMessage()).contains("적립되었습니다");
        });
    }

    @Test
    @DisplayName("일일 정산 실패 이벤트를 수신하면 일일 정산 실패 알림이 생성된다")
    void handleDailySettlementFailedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        SettlementEvent event = new SettlementEvent(
                UUID.randomUUID(),
                sellerId,
                java.time.OffsetDateTime.now().minusDays(1),
                java.time.OffsetDateTime.now(),
                SettlementEvent.Status.FAILED,
                30000L,
                java.math.BigDecimal.valueOf(300),
                java.math.BigDecimal.valueOf(29700)
        );

        // when
        kafkaTemplate.send(KafkaTopics.DAILY_SETTLEMENT_FAILED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.DAILY_SETTLEMENT_FAILED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("일일 정산 실패");
            assertThat(notification.getMessage()).contains("29,700원");
            assertThat(notification.getMessage()).contains("다음날 같이 적립될 예정");
        });
    }

    @Test
    @DisplayName("월간 정산 완료 이벤트를 수신하면 월간 정산 완료 알림이 생성된다")
    void handleMonthlySettlementCompletedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        SettlementEvent event = new SettlementEvent(
                UUID.randomUUID(),
                sellerId,
                java.time.OffsetDateTime.now().minusMonths(1),
                java.time.OffsetDateTime.now(),
                SettlementEvent.Status.SUCCESS,
                1000000L,
                java.math.BigDecimal.valueOf(10000),
                java.math.BigDecimal.valueOf(990000)
        );

        // when
        kafkaTemplate.send(KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.MONTHLY_SETTLEMENT_COMPLETED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("월간 정산 완료");
            assertThat(notification.getMessage()).contains("990,000원");
            assertThat(notification.getMessage()).contains("송금되었습니다");
        });
    }

    @Test
    @DisplayName("월간 정산 실패 이벤트를 수신하면 월간 정산 실패 알림이 생성된다")
    void handleMonthlySettlementFailedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        SettlementEvent event = new SettlementEvent(
                UUID.randomUUID(),
                sellerId,
                java.time.OffsetDateTime.now().minusMonths(1),
                java.time.OffsetDateTime.now(),
                SettlementEvent.Status.FAILED,
                800000L,
                java.math.BigDecimal.valueOf(8000),
                java.math.BigDecimal.valueOf(792000)
        );

        // when
        kafkaTemplate.send(KafkaTopics.MONTHLY_SETTLEMENT_FAILED, sellerId.toString(), event);

        // then
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository
                    .findByMemberIdAndStatus(sellerId, NotificationStatus.SENT);

            assertThat(notifications).hasSizeGreaterThanOrEqualTo(1);
            Notification notification = notifications.stream()
                    .filter(n -> n.getType() == NotificationType.MONTHLY_SETTLEMENT_FAILED)
                    .findFirst()
                    .orElseThrow();
            assertThat(notification.getTitle()).isEqualTo("월간 정산 실패");
            assertThat(notification.getMessage()).contains("792,000원");
            assertThat(notification.getMessage()).contains("송금되지 않았습니다");
        });
    }
}
