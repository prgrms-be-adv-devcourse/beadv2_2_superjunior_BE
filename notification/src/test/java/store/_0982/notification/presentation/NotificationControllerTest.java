package store._0982.notification.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.notification.application.NotificationService;
import store._0982.notification.application.dto.NotificationInfo;
import store._0982.notification.domain.NotificationStatus;
import store._0982.notification.domain.NotificationType;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public NotificationService notificationService() {
            return mock(NotificationService.class);
        }
    }

    @Test
    @DisplayName("알림을 읽음 처리한다")
    void read() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        doNothing().when(notificationService).read(memberId, notificationId);

        // when & then
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("정상적으로 읽었습니다."));

        verify(notificationService).read(memberId, notificationId);
    }

    @Test
    @DisplayName("알림 전체를 읽음 처리한다")
    void readAll() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();

        doNothing().when(notificationService).readAll(memberId);

        // when & then
        mockMvc.perform(patch("/api/notifications/read")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("모든 알림을 정상적으로 읽었습니다."));

        verify(notificationService).readAll(memberId);
    }

    @Test
    @DisplayName("알림 목록을 조회한다")
    void getNotifications() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<NotificationInfo> notifications = List.of(
                new NotificationInfo(
                        UUID.randomUUID(),
                        memberId,
                        NotificationType.POINT_RECHARGED,
                        "포인트 충전",
                        "10,000원이 충전되었습니다.",
                        null,
                        NotificationStatus.SENT,
                        UUID.randomUUID()
                ),
                new NotificationInfo(
                        UUID.randomUUID(),
                        memberId,
                        NotificationType.ORDER_COMPLETED,
                        "주문 완료",
                        "주문이 완료되었습니다.",
                        null,
                        NotificationStatus.READ,
                        UUID.randomUUID()
                )
        );

        PageResponse<NotificationInfo> pageResponse = PageResponse.from(
                new PageImpl<>(notifications, PageRequest.of(0, 20), notifications.size())
        );

        when(notificationService.getNotifications(eq(memberId), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/notifications")
                        .header(HeaderName.ID, memberId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("알림 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(notificationService).getNotifications(eq(memberId), any(Pageable.class));
    }

    @Test
    @DisplayName("읽지 않은 알림 목록을 조회한다")
    void getUnreadNotifications() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<NotificationInfo> notifications = List.of(
                new NotificationInfo(
                        UUID.randomUUID(),
                        memberId,
                        NotificationType.POINT_RECHARGED,
                        "포인트 충전",
                        "10,000원이 충전되었습니다.",
                        null,
                        NotificationStatus.SENT,
                        UUID.randomUUID()
                )
        );

        PageResponse<NotificationInfo> pageResponse = PageResponse.from(
                new PageImpl<>(notifications, PageRequest.of(0, 20), notifications.size())
        );

        when(notificationService.getUnreadNotifications(eq(memberId), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/notifications/unread")
                        .header(HeaderName.ID, memberId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("읽지 않은 알림 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("SENT"));

        verify(notificationService).getUnreadNotifications(eq(memberId), any(Pageable.class));
    }
}
