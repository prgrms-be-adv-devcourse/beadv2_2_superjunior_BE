package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.point.application.dto.PgConfirmCommand;
import store._0982.point.application.dto.PgCreateInfo;
import store._0982.point.application.dto.PgFailCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.pg.PgConfirmService;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.presentation.dto.PgConfirmRequest;
import store._0982.point.presentation.dto.PgCreateRequest;
import store._0982.point.presentation.dto.PgFailRequest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PgPaymentController.class)
class PgPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PgPaymentService pgPaymentService;

    @MockitoBean
    private PgFailService pgFailService;

    @MockitoBean
    private PgCancelService pgCancelService;

    @MockitoBean
    private PgConfirmService pgConfirmService;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPayment() throws Exception {
        // given
        PgCreateRequest request = new PgCreateRequest(orderId, 10000);

        when(pgPaymentService.createPayment(any(), eq(memberId))).thenReturn(any(PgCreateInfo.class));

        // when & then
        mockMvc.perform(post("/api/payments/create")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(pgPaymentService).createPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전을 완료한다")
    void confirmPayment() throws Exception {
        // given
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        doNothing().when(pgConfirmService).confirmPayment(any(PgConfirmCommand.class), eq(memberId));

        // when & then
        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(pgConfirmService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 완료 중 에러가 발생한다")
    void confirmPayment_fail() throws Exception {
        // given
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        doThrow(new RuntimeException("임의 에러")).when(pgConfirmService).confirmPayment(any(), eq(memberId));

        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(pgConfirmService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 결제 실패를 처리한다")
    void handlePaymentFailure() throws Exception {
        // given
        PgFailRequest request = new PgFailRequest(
                orderId, "test_payment_key", "PAYMENT_FAILED", "카드 승인 실패", 10000, "{}"
        );

        doNothing().when(pgFailService).handlePaymentFailure(any(PgFailCommand.class), eq(memberId));

        // when & then
        mockMvc.perform(post("/api/payments/fail")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(pgFailService).handlePaymentFailure(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 내역을 조회한다")
    void getPaymentHistories() throws Exception {
        // given
        when(pgPaymentService.getPaymentHistories(eq(memberId), any(Pageable.class))).thenReturn(any());

        // when & then
        mockMvc.perform(get("/api/payments")
                        .header(HeaderName.ID, memberId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.data.content").isArray());

        verify(pgPaymentService).getPaymentHistories(eq(memberId), any(Pageable.class));
    }
}
