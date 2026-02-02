package store._0982.point.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.point.PointPaymentService;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalPointController.class)
class InternalPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointPaymentService pointPaymentService;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PointPaymentService pointPaymentService() {
            return mock(PointPaymentService.class);
        }
    }

    @Test
    @DisplayName("회원 가입 시 포인트 잔액을 초기화한다")
    void initializeBalance() throws Exception {
        // given
        PointBalanceInfo balanceInfo = new PointBalanceInfo(
                memberId,
                0,
                0,
                OffsetDateTime.now()
        );

        when(pointPaymentService.initializeBalance(memberId)).thenReturn(balanceInfo);

        // when & then
        mockMvc.perform(post("/internal/points")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.paidPoint").value(0))
                .andExpect(jsonPath("$.data.bonusPoint").value(0));

        verify(pointPaymentService).initializeBalance(memberId);
    }

    @Test
    @DisplayName("회원 가입 실패 시 포인트 잔액 초기화를 롤백한다")
    void rollbackBalance() throws Exception {
        // given
        doNothing().when(pointPaymentService).rollbackBalance(memberId);

        // when & then
        mockMvc.perform(delete("/internal/points")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(pointPaymentService).rollbackBalance(memberId);
    }
}
