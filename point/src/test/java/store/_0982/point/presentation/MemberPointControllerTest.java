package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.presentation.dto.PointDeductRequest;
import store._0982.point.presentation.dto.PointReturnRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberPointController.class)
class MemberPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberPointService memberPointService;

    @Autowired
    private ObjectMapper objectMapper;

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
        MemberPointInfo info = new MemberPointInfo(memberId, 15000, OffsetDateTime.now());

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

    @Test
    @DisplayName("포인트를 차감한다")
    void deductPoints() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        PointDeductRequest request = new PointDeductRequest(idempotencyKey, orderId, 5000);
        MemberPointInfo info = new MemberPointInfo(memberId, 5000, OffsetDateTime.now());

        when(memberPointService.deductPoints(eq(memberId), any())).thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/points/deduct")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("포인트 차감 완료"))
                .andExpect(jsonPath("$.data.pointBalance").value(5000));

        verify(memberPointService).deductPoints(eq(memberId), any());
    }

    @Test
    @DisplayName("포인트를 반환한다")
    void returnPoints() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        PointReturnRequest request = new PointReturnRequest(idempotencyKey, orderId, 5000);
        MemberPointInfo info = new MemberPointInfo(memberId, 15000, OffsetDateTime.now());

        when(memberPointService.returnPoints(eq(memberId), any())).thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/points/return")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("포인트 반환 완료"))
                .andExpect(jsonPath("$.data.pointBalance").value(15000));

        verify(memberPointService).returnPoints(eq(memberId), any());
    }
}
