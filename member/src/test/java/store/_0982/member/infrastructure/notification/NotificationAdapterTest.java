package store._0982.member.infrastructure.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.domain.notification.constant.NotificationType;
import store._0982.member.domain.notification.constant.ReferenceType;
import store._0982.member.support.BaseIntegrationTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationAdapterTest extends BaseIntegrationTest {

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private NotificationAdapter notificationAdapter;

    @Test
    @DisplayName("ID로 알림을 조회한다")
    void findById() {
        // given
        Notification notification = createSampleNotification(UUID.randomUUID());
        Notification saved = notificationJpaRepository.save(notification);

        // when
        Optional<Notification> result = notificationAdapter.findById(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getTitle()).isEqualTo(notification.getTitle());
    }

    @Test
    @DisplayName("회원 ID와 상태로 알림 목록을 조회한다")
    void findByMemberIdAndStatus() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationStatus status = NotificationStatus.SENT;

        Notification notification1 = createSampleNotificationWithMemberId(memberId, status);
        Notification notification2 = createSampleNotificationWithMemberId(memberId, status);

        notificationJpaRepository.save(notification1);
        notificationJpaRepository.save(notification2);

        // when
        List<Notification> result = notificationAdapter.findByMemberIdAndStatus(memberId, status);

        // then
        assertThat(result).hasSize(2)
                .allMatch(n -> n.getMemberId().equals(memberId))
                .allMatch(n -> n.getStatus().equals(status));
    }

    @Test
    @DisplayName("회원 ID로 페이징된 알림 목록을 조회한다")
    void findByMemberId() {
        // given
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        Notification notification1 = createSampleNotificationWithMemberId(memberId, NotificationStatus.SENT);
        Notification notification2 = createSampleNotificationWithMemberId(memberId, NotificationStatus.READ);

        notificationJpaRepository.save(notification1);
        notificationJpaRepository.save(notification2);

        // when
        Page<Notification> result = notificationAdapter.findByMemberIdAndChannel(memberId, NotificationChannel.IN_APP, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(n -> n.getMemberId().equals(memberId));
    }

    @Test
    @DisplayName("회원 ID와 상태로 페이징된 알림 목록을 조회한다")
    void findByMemberIdAndStatusWithPageable() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationStatus status = NotificationStatus.SENT;
        Pageable pageable = PageRequest.of(0, 20);

        Notification notification = createSampleNotificationWithMemberId(memberId, status);
        notificationJpaRepository.save(notification);

        // when
        Page<Notification> result = notificationAdapter.findByMemberIdAndStatusAndChannel(
                memberId, status, NotificationChannel.IN_APP, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMemberId()).isEqualTo(memberId);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("알림을 저장한다")
    void save() {
        // given
        Notification notification = createSampleNotification(UUID.randomUUID());

        // when
        notificationAdapter.save(notification);

        // then
        Optional<Notification> saved = notificationJpaRepository.findById(notification.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitle()).isEqualTo(notification.getTitle());
        assertThat(saved.get().getMessage()).isEqualTo(notification.getMessage());
    }

    private Notification createSampleNotification(UUID id) {
        return Notification.builder()
                .id(id)
                .memberId(UUID.randomUUID())
                .type(NotificationType.POINT_RECHARGED)
                .channel(NotificationChannel.IN_APP)
                .title("테스트 알림")
                .message("테스트 메시지")
                .referenceType(ReferenceType.POINT)
                .referenceId(UUID.randomUUID())
                .status(NotificationStatus.SENT)
                .build();
    }

    private Notification createSampleNotificationWithMemberId(UUID memberId, NotificationStatus status) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .memberId(memberId)
                .type(NotificationType.POINT_RECHARGED)
                .channel(NotificationChannel.IN_APP)
                .title("테스트 알림")
                .message("테스트 메시지")
                .referenceType(ReferenceType.POINT)
                .referenceId(UUID.randomUUID())
                .status(status)
                .build();
    }
}
