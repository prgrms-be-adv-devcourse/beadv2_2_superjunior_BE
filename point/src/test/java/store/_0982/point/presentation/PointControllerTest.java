package store._0982.point.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.point.application.MemberPointService;
import store._0982.point.application.dto.PointInfo;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberPointService memberPointService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MemberPointService memberPointService() {
            return mock(MemberPointService.class);
        }
    }

    @Test
    @DisplayName("보유 포인트를 조회한다")
    void getPoints() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        PointInfo info = new PointInfo(memberId, 15000, OffsetDateTime.now());

        when(memberPointService.getPoints(memberId)).thenReturn(info);

        // when & then
        mockMvc.perform(get("/api/points")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("포인트 조회 성공"))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.pointBalance").value(15000));

        verify(memberPointService).getPoints(memberId);
    }
}
