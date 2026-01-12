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
import store._0982.point.application.payment.PaymentFailService;
import store._0982.point.application.payment.PaymentService;
import store._0982.point.application.payment.PaymentRefundService;
import store._0982.point.application.dto.*;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.presentation.dto.PaymentConfirmRequest;
import store._0982.point.presentation.dto.PaymentCreateRequest;
import store._0982.point.presentation.dto.PaymentFailRequest;
import store._0982.point.presentation.dto.PointRefundRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    private static final String SUCCESS_URL_PATTERN = "**/point/charge/success**";
    private static final String FAIL_URL_PATTERN = "**/point/charge/fail**";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private PaymentFailService paymentFailService;

    @MockitoBean
    private PaymentRefundService paymentRefundService;

    @MockitoBean
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(paymentService);
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPayment() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentCreateRequest request = new PaymentCreateRequest(orderId, 10000);

        PaymentCreateInfo info = new PaymentCreateInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                10000,
                PaymentStatus.PENDING,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(paymentService.createPayment(any(), eq(memberId))).thenReturn(info);

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

        verify(paymentService).createPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전을 완료한다")
    void confirmPayment() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentConfirmRequest request = new PaymentConfirmRequest(orderId, 10000, "test_payment_key");

        PaymentInfo paymentInfo = new PaymentInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                "카드",
                "test_payment_key",
                null,
                10000,
                PaymentStatus.COMPLETED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(paymentService.confirmPayment(any(PaymentConfirmCommand.class), eq(memberId))).thenReturn(paymentInfo);

        // when & then
        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(SUCCESS_URL_PATTERN));

        verify(paymentService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 완료 중 에러가 발생한다")
    void confirmPayment_fail() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentConfirmRequest request = new PaymentConfirmRequest(orderId, 10000, "test_payment_key");

        doThrow(new RuntimeException("임의 에러")).when(paymentService).confirmPayment(any(), eq(memberId));

        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(FAIL_URL_PATTERN));

        verify(paymentService).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 결제 실패를 처리한다")
    void handlePaymentFailure() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentFailRequest request = new PaymentFailRequest(
                orderId, "test_payment_key", "PAYMENT_FAILED", "카드 승인 실패", 10000, "{}"
        );

        when(paymentFailService.handlePaymentFailure(any(PaymentFailCommand.class), eq(memberId)))
                .thenReturn(mock(PaymentInfo.class));

        // when & then
        mockMvc.perform(post("/api/payments/fail")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern(FAIL_URL_PATTERN));

        verify(paymentFailService).handlePaymentFailure(any(), eq(memberId));
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
                PaymentStatus.REFUNDED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(paymentRefundService.refundPaymentPoint(eq(memberId), any())).thenReturn(info);

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

        verify(paymentRefundService).refundPaymentPoint(eq(memberId), any());
    }

    @Test
    @DisplayName("포인트 충전 내역을 조회한다")
    void getPaymentHistories() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<PaymentInfo> histories = List.of(
                new PaymentInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_1",
                        null,
                        10000,
                        PaymentStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new PaymentInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_2",
                        null,
                        20000,
                        PaymentStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        PageResponse<PaymentInfo> pageResponse = PageResponse.from(
                new PageImpl<>(histories, PageRequest.of(0, 20), histories.size())
        );

        when(paymentService.getPaymentHistories(eq(memberId), any(Pageable.class)))
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

        verify(paymentService).getPaymentHistories(eq(memberId), any(Pageable.class));
    }
}
