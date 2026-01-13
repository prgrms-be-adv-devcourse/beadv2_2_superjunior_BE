package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.application.dto.*;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.presentation.dto.PgConfirmRequest;
import store._0982.point.presentation.dto.PgCreateRequest;
import store._0982.point.presentation.dto.PgFailRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PgPaymentController.class)
class PgPaymentControllerTest {
    private static final String SUCCESS_URL_PATTERN = "**/point/charge/success**";
    private static final String FAIL_URL_PATTERN = "**/point/charge/fail**";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PgPaymentService pgPaymentService;

    @MockitoBean
    private PgFailService pgFailService;

    @MockitoBean
    private PgCancelService pgCancelService;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(pgPaymentService);
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPayment() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgCreateRequest request = new PgCreateRequest(orderId, 10000);

        PgCreateInfo info = new PgCreateInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                10000,
                PgPaymentStatus.PENDING,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(pgPaymentService.createPayment(any(), eq(memberId))).thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/payments/create")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("결제 요청 생성"))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.amount").value(10000));

        verify(pgPaymentService).createPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전을 완료한다")
    void confirmPayment() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        PgPaymentInfo pgPaymentInfo = new PgPaymentInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                "카드",
                "test_payment_key",
                null,
                10000,
                PgPaymentStatus.COMPLETED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(pgPaymentService.confirmPayment(any(PgConfirmCommand.class), eq(memberId))).thenReturn(pgPaymentInfo);

        // when & then
        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(SUCCESS_URL_PATTERN));

        verify(pgPaymentService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 완료 중 에러가 발생한다")
    void confirmPayment_fail() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        doThrow(new RuntimeException("임의 에러")).when(pgPaymentService).confirmPayment(any(), eq(memberId));

        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(FAIL_URL_PATTERN));

        verify(pgPaymentService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 결제 실패를 처리한다")
    void handlePaymentFailure() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgFailRequest request = new PgFailRequest(
                orderId, "test_payment_key", "PAYMENT_FAILED", "카드 승인 실패", 10000, "{}"
        );

        when(pgFailService.handlePaymentFailure(any(PgFailCommand.class), eq(memberId)))
                .thenReturn(mock(PgPaymentInfo.class));

        // when & then
        mockMvc.perform(post("/api/payments/fail")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(FAIL_URL_PATTERN));

        verify(pgFailService).handlePaymentFailure(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트를 환불한다")
    void refundPaymentPoint() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PointRefundRequest request = new PointRefundRequest(orderId, "환불 요청");

        PointRefundInfo info = new PointRefundInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                "CARD",
                "test_payment_key",
                10000,
                PgPaymentStatus.REFUNDED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(pgCancelService.refundPaymentPoint(eq(memberId), any())).thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/payments/refund")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("포인트 결제 환불 완료"))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.amount").value(10000));

        verify(pgCancelService).refundPaymentPoint(eq(memberId), any());
    }

    @Test
    @DisplayName("포인트 충전 내역을 조회한다")
    void getPaymentHistories() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<PgPaymentInfo> histories = List.of(
                new PgPaymentInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_1",
                        null,
                        10000,
                        PgPaymentStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new PgPaymentInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_2",
                        null,
                        20000,
                        PgPaymentStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        PageResponse<PgPaymentInfo> pageResponse = PageResponse.from(
                new PageImpl<>(histories, PageRequest.of(0, 20), histories.size())
        );

        when(pgPaymentService.getPaymentHistories(eq(memberId), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/payments")
                        .header(HeaderName.ID, memberId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("포인트 충전 내역 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));

        verify(pgPaymentService).getPaymentHistories(eq(memberId), any(Pageable.class));
    }
}
