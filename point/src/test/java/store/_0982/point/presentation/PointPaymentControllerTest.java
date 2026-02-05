package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.common.HeaderName;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointChargeCommand;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.application.dto.point.PointTransactionInfo;
import store._0982.point.application.dto.point.PointTransferCommand;
import store._0982.point.application.point.*;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.constant.PointType;
import store._0982.point.presentation.dto.PointChargeRequest;
import store._0982.point.presentation.dto.PointDeductRequest;
import store._0982.point.presentation.dto.PointTransferRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointPaymentController.class)
class PointPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointPaymentService pointPaymentService;

    @Autowired
    private PointChargeService pointChargeService;

    @Autowired
    private PointDeductFacade pointDeductFacade;

    @Autowired
    private PointTransferService pointTransferService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID memberId;
    private UUID idempotencyKey;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public PointPaymentService pointPaymentService() {
            return mock(PointPaymentService.class);
        }

        @Bean
        public PointChargeService pointChargeService() {
            return mock(PointChargeService.class);
        }

        @Bean
        public PointDeductFacade pointDeductFacade() {
            return mock(PointDeductFacade.class);
        }

        @Bean
        public PointTransferService pointTransferService() {
            return mock(PointTransferService.class);
        }
    }

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Test
    @DisplayName("보유 포인트를 조회한다")
    void getPoints() throws Exception {
        // given
        PointBalanceInfo info = new PointBalanceInfo(memberId, 15000, 0, OffsetDateTime.now());

        when(pointPaymentService.getPoints(memberId)).thenReturn(info);

        // when & then
        mockMvc.perform(get("/api/points")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.paidPoint").value(15000))
                .andExpect(jsonPath("$.data.bonusPoint").value(0));

        verify(pointPaymentService).getPoints(memberId);
    }

    @Test
    @DisplayName("포인트를 충전한다")
    void chargePoints() throws Exception {
        // given
        long amount = 10000;
        PointChargeRequest request = new PointChargeRequest(amount, idempotencyKey);
        PointBalanceInfo info = new PointBalanceInfo(memberId, amount, 0, OffsetDateTime.now());

        when(pointChargeService.chargePoints(eq(memberId), any(PointChargeCommand.class)))
                .thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/points/charge")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.paidPoint").value(amount));

        verify(pointChargeService).chargePoints(eq(memberId), any(PointChargeCommand.class));
    }

    @Test
    @DisplayName("포인트로 결제한다")
    void deductPoints() throws Exception {
        // given
        UUID orderId = UUID.randomUUID();
        long amount = 5000;
        String groupPurchaseName = "테스트 공구";
        PointDeductRequest request = new PointDeductRequest(
                idempotencyKey, orderId, amount, groupPurchaseName, false
        );
        PointBalanceInfo info = new PointBalanceInfo(memberId, 5000, 0, OffsetDateTime.now());

        when(pointDeductFacade.deductPoints(eq(memberId), any(PointDeductCommand.class)))
                .thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/points/deduct")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.paidPoint").value(5000));

        verify(pointDeductFacade).deductPoints(eq(memberId), any(PointDeductCommand.class));
    }

    @Test
    @DisplayName("포인트 충전/차감 이력을 조회한다")
    void getTransactions() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        PointTransactionInfo transactionInfo = PointTransactionInfo.builder()
                .id(UUID.randomUUID())
                .status(PointTransactionStatus.CHARGED)
                .amount(10000)
                .orderId(null)
                .cancelReason(null)
                .groupPurchaseName(null)
                .build();

        Page<PointTransactionInfo> page = new PageImpl<>(List.of(transactionInfo), pageable, 1);

        when(pointPaymentService.getTransactions(eq(memberId), eq(PointType.PAID), any(Pageable.class)))
                .thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/points/histories")
                        .header(HeaderName.ID, memberId.toString())
                        .param("type", "PAID")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].status").value("CHARGED"))
                .andExpect(jsonPath("$.data.content[0].amount").value(10000));

        verify(pointPaymentService).getTransactions(eq(memberId), eq(PointType.PAID), any(Pageable.class));
    }

    @Test
    @DisplayName("포인트를 출금한다")
    void transfer() throws Exception {
        // given
        long amount = 20000;
        PointTransferRequest request = new PointTransferRequest(amount, idempotencyKey);
        PointBalanceInfo info = new PointBalanceInfo(memberId, 0, 0, OffsetDateTime.now());

        when(pointTransferService.transfer(eq(memberId), any(PointTransferCommand.class)))
                .thenReturn(info);

        // when & then
        mockMvc.perform(post("/api/points/transfer")
                        .header(HeaderName.ID, memberId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                .andExpect(jsonPath("$.data.paidPoint").value(0));

        verify(pointTransferService).transfer(eq(memberId), any(PointTransferCommand.class));
    }
}
