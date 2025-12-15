package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import store._0982.point.application.PaymentPointService;
import store._0982.point.application.RefundService;
import store._0982.point.application.dto.*;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.presentation.dto.PointChargeCreateRequest;
import store._0982.point.presentation.dto.PointRefundRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentPointController.class)
class PaymentPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentPointService paymentPointService;

    @Autowired
    private RefundService refundService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PaymentPointService paymentPointService() {
            return mock(PaymentPointService.class);
        }

        @Bean
        public RefundService refundService() {
            return mock(RefundService.class);
        }

        @Bean
        public OrderServiceClient orderServiceClient() {
            return mock(OrderServiceClient.class);
        }
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPaymentPoint() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PointChargeCreateRequest request = new PointChargeCreateRequest(orderId, 10000);

        PaymentPointCreateInfo info = new PaymentPointCreateInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                10000,
                PaymentPointStatus.REQUESTED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(paymentPointService.createPaymentPoint(any(), eq(memberId))).thenReturn(info);

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

        verify(paymentPointService).createPaymentPoint(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전을 완료한다")
    void confirmPayment() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        PaymentPointInfo paymentPointInfo = new PaymentPointInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                "CARD",
                "test_payment_key",
                10000,
                PaymentPointStatus.COMPLETED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        MemberPointInfo memberPointInfo = new MemberPointInfo(memberId, 10000, OffsetDateTime.now());

        PointChargeConfirmInfo info = new PointChargeConfirmInfo(paymentPointInfo, memberPointInfo);

        when(paymentPointService.confirmPayment(any())).thenReturn(info);

        // when & then
        mockMvc.perform(get("/api/payments/confirm")
                        .param("paymentKey", "test_payment_key")
                        .param("orderId", orderId.toString())
                        .param("amount", "10000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("결제 및 포인트 충전 성공"))
                .andExpect(jsonPath("$.data.paymentPoint.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.memberPoint.pointBalance").value(10000));

        verify(paymentPointService).confirmPayment(any());
    }

    @Test
    @DisplayName("포인트 결제 실패를 처리한다")
    void handlePaymentFailure() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        PointChargeFailInfo info = new PointChargeFailInfo(
                UUID.randomUUID(),
                orderId,
                "test_payment_key",
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000,
                OffsetDateTime.now()
        );

        when(paymentPointService.handlePaymentFailure(any())).thenReturn(info);

        // when & then
        mockMvc.perform(get("/api/payments/fail")
                        .param("errorCode", "PAYMENT_FAILED")
                        .param("errorMessage", "카드 승인 실패")
                        .param("orderId", orderId.toString())
                        .param("paymentKey", "test_payment_key")
                        .param("amount", "10000")
                        .param("rawPayload", "{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("결제 실패 정보 저장 완료"))
                .andExpect(jsonPath("$.data.errorCode").value("PAYMENT_FAILED"));

        verify(paymentPointService).handlePaymentFailure(any());
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
                PaymentPointStatus.REFUNDED,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(refundService.refundPaymentPoint(eq(memberId), any())).thenReturn(info);

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

        verify(refundService).refundPaymentPoint(eq(memberId), any());
    }

    @Test
    @DisplayName("포인트 충전 내역을 조회한다")
    void getPaymentHistory() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        List<PaymentPointHistoryInfo> histories = List.of(
                new PaymentPointHistoryInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_1",
                        10000,
                        PaymentPointStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                ),
                new PaymentPointHistoryInfo(
                        UUID.randomUUID(),
                        memberId,
                        UUID.randomUUID(),
                        "CARD",
                        "payment_key_2",
                        20000,
                        PaymentPointStatus.COMPLETED,
                        OffsetDateTime.now(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now()
                )
        );

        PageResponse<PaymentPointHistoryInfo> pageResponse = PageResponse.from(
                new PageImpl<>(histories, PageRequest.of(0, 20), histories.size())
        );

        when(paymentPointService.getPaymentHistory(eq(memberId), any(Pageable.class)))
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

        verify(paymentPointService).getPaymentHistory(eq(memberId), any(Pageable.class));
    }
}
