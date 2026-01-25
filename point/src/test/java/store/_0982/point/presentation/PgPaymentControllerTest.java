package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.application.dto.pg.PgCreateInfo;
import store._0982.point.application.dto.pg.PgFailCommand;
import store._0982.point.application.dto.pg.PgPaymentInfo;
import store._0982.point.application.pg.PgConfirmFacade;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.domain.constant.PaymentMethod;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PgPaymentController.class)
class PgPaymentControllerTest {

    private static final String PURCHASE_NAME = "테스트 공구";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PgPaymentService pgPaymentService;

    @Autowired
    private PgFailService pgFailService;

    @Autowired
    private PgConfirmFacade pgConfirmFacade;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID memberId;
    private UUID orderId;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PgPaymentService pgPaymentService() {
            return mock(PgPaymentService.class);
        }

        @Bean
        public PgFailService pgFailService() {
            return mock(PgFailService.class);
        }

        @Bean
        public PgConfirmFacade pgConfirmService() {
            return mock(PgConfirmFacade.class);
        }
    }

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트 충전 주문을 생성한다")
    void createPayment() throws Exception {
        // given
        PgCreateRequest request = new PgCreateRequest(orderId, 10000, PURCHASE_NAME);
        PgCreateInfo createInfo = new PgCreateInfo(
                UUID.randomUUID(),
                memberId,
                orderId,
                10000L,
                PgPaymentStatus.PENDING,
                OffsetDateTime.now(),
                null
        );

        when(pgPaymentService.createPayment(any(), eq(memberId))).thenReturn(createInfo);

        // when & then
        mockMvc.perform(post("/api/payments/create")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(pgPaymentService).createPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전을 완료한다")
    void confirmPayment() throws Exception {
        // given
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        doNothing().when(pgConfirmFacade).confirmPayment(any(PgConfirmCommand.class), eq(memberId));

        // when & then
        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(pgConfirmFacade).confirmPayment(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 완료 중 에러가 발생한다")
    void confirmPayment_fail() throws Exception {
        // given
        PgConfirmRequest request = new PgConfirmRequest(orderId, 10000, "test_payment_key");

        doThrow(new RuntimeException("임의 에러")).when(pgConfirmFacade).confirmPayment(any(), eq(memberId));

        mockMvc.perform(post("/api/payments/confirm")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        verify(pgConfirmFacade).confirmPayment(any(), eq(memberId));
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
                .andExpect(status().isAccepted());

        verify(pgFailService).handlePaymentFailure(any(), eq(memberId));
    }

    @Test
    @DisplayName("포인트 충전 내역을 조회한다")
    void getPaymentHistories() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 20);

        PgPaymentInfo paymentInfo = new PgPaymentInfo(
                UUID.randomUUID(),
                memberId,
                UUID.randomUUID(),
                PaymentMethod.CARD,
                "test_payment_key",
                10000L,
                PgPaymentStatus.COMPLETED,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                PURCHASE_NAME
        );

        List<PgPaymentInfo> content = List.of(paymentInfo);
        PageResponse<PgPaymentInfo> pageResponse = PageResponse.from(
                new PageImpl<>(content, pageable, 1)
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
                .andExpect(jsonPath("$.data.content").isArray());

        verify(pgPaymentService).getPaymentHistories(eq(memberId), any(Pageable.class));
    }

    @Test
    @DisplayName("포인트 충전 내역을 상세 조회한다")
    void getPaymentHistory() throws Exception {
        // given
        UUID paymentId = UUID.randomUUID();
        PgPaymentInfo paymentInfo = new PgPaymentInfo(
                paymentId,
                memberId,
                orderId,
                PaymentMethod.CARD,
                "test_payment_key",
                10000L,
                PgPaymentStatus.COMPLETED,
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                PURCHASE_NAME
        );

        when(pgPaymentService.getPaymentHistory(paymentId, memberId))
                .thenReturn(paymentInfo);

        // when & then
        mockMvc.perform(get("/api/payments/{id}", paymentId)
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.paymentPointId").value(paymentId.toString()))
                .andExpect(jsonPath("$.data.amount").value(10000));

        verify(pgPaymentService).getPaymentHistory(paymentId, memberId);
    }
}
