package store._0982.member.presentation.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.application.notification.dto.NotificationSettingInfo;
import store._0982.member.config.member.SecurityConfig;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.presentation.notification.dto.NotificationSettingUpdateRequest;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationSettingController.class)
@Import(SecurityConfig.class)
class NotificationSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("알림 설정을 전체 조회한다")
    void getNotificationSettings() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<NotificationSettingInfo> settings = List.of(
                NotificationSettingInfo.of(NotificationChannel.EMAIL, true),
                NotificationSettingInfo.of(NotificationChannel.IN_APP, true)
        );

        when(notificationSettingService.getAllSettings(memberId)).thenReturn(settings);

        // when & then
        mockMvc.perform(get("/api/notification-settings")
                        .header(HeaderName.ID, memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].channel").value("EMAIL"))
                .andExpect(jsonPath("$.data[0].isEnabled").value(true))
                .andExpect(jsonPath("$.data[1].channel").value("IN_APP"))
                .andExpect(jsonPath("$.data[1].isEnabled").value(true));

        verify(notificationSettingService).getAllSettings(memberId);
    }

    @Test
    @DisplayName("알림 설정을 일괄 변경한다")
    void updateNotificationSettings() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<NotificationSettingUpdateRequest> requests = List.of(
                new NotificationSettingUpdateRequest(NotificationChannel.EMAIL, false),
                new NotificationSettingUpdateRequest(NotificationChannel.IN_APP, false) // Service에서 true로 강제 변환됨
        );

        // when & then
        mockMvc.perform(put("/api/notification-settings")
                        .header(HeaderName.ID, memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(notificationSettingService).updateSettings(eq(memberId), any());
    }
}
