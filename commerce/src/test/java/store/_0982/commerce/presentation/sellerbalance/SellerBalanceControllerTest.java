package store._0982.commerce.presentation.sellerbalance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceInfo;
import store._0982.common.HeaderName;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import store._0982.common.dto.PageResponse;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SellerBalanceController.class)
class SellerBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SellerBalanceService sellerBalanceService;

    @BeforeEach
    void setUp() {
        reset(sellerBalanceService);
    }

    @Nested
    @DisplayName("balance 조회 API")
    class GetBalanceTest {

        @Test
        @DisplayName("판매자 balance를 조회합니다.")
        void getBalance_success() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            UUID balanceId = UUID.randomUUID();
            SellerBalanceInfo sellerBalanceInfo = new SellerBalanceInfo(
                    balanceId,
                    memberId,
                    150000L
            );

            when(sellerBalanceService.getBalance(memberId))
                    .thenReturn(sellerBalanceInfo);

            // when & then
            mockMvc.perform(
                            get("/api/balances")
                                    .header(HeaderName.ID, memberId.toString())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("조회되었습니다."))
                    .andExpect(jsonPath("$.data.sellerBalanceId").value(balanceId.toString()))
                    .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.data.balance").value(150000));

            verify(sellerBalanceService, times(1))
                    .getBalance(memberId);
        }

        @Test
        @DisplayName("balance가 0원일 때도 정상 조회됩니다.")
        void getBalance_zeroBalance() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            UUID balanceId = UUID.randomUUID();
            SellerBalanceInfo sellerBalanceInfo = new SellerBalanceInfo(
                    balanceId,
                    memberId,
                    0L
            );

            when(sellerBalanceService.getBalance(memberId))
                    .thenReturn(sellerBalanceInfo);

            // when & then
            mockMvc.perform(
                            get("/api/balances")
                                    .header(HeaderName.ID, memberId.toString())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("조회되었습니다."))
                    .andExpect(jsonPath("$.data.sellerBalanceId").value(balanceId.toString()))
                    .andExpect(jsonPath("$.data.memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.data.balance").value(0));

            verify(sellerBalanceService, times(1))
                    .getBalance(memberId);
        }
    }

    @Nested
    @DisplayName("balance 변동 내역 조회 API")
    class GetBalanceHistoryTest {

        @Test
        @DisplayName("balance 변동 내역을 페이징하여 조회합니다.")
        void getBalanceHistory_success() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            UUID settlementId1 = UUID.randomUUID();
            UUID settlementId2 = UUID.randomUUID();

            List<SellerBalanceHistoryInfo> histories = List.of(
                    new SellerBalanceHistoryInfo(
                            UUID.randomUUID(),
                            memberId,
                            settlementId1,
                            50000L,
                            SellerBalanceHistoryStatus.CREDIT,
                            OffsetDateTime.now().minusDays(1)
                    ),
                    new SellerBalanceHistoryInfo(
                            UUID.randomUUID(),
                            memberId,
                            settlementId2,
                            20000L,
                            SellerBalanceHistoryStatus.DEBIT,
                            OffsetDateTime.now()
                    )
            );

            PageResponse<SellerBalanceHistoryInfo> pageResponse = PageResponse.from(
                    new PageImpl<>(histories, PageRequest.of(0, 20), 2)
            );

            when(sellerBalanceService.getBalanceHistory(eq(memberId), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(
                            get("/api/balances/history")
                                    .header(HeaderName.ID, memberId.toString())
                                    .param("page", "0")
                                    .param("size", "20")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("조회되었습니다."))

                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.size").value(20))
                    .andExpect(jsonPath("$.data.numberOfElements").value(2))

                    .andExpect(jsonPath("$.data.content[0].memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.data.content[0].amount").value(50000))
                    .andExpect(jsonPath("$.data.content[0].status").value("CREDIT"))

                    .andExpect(jsonPath("$.data.content[1].memberId").value(memberId.toString()))
                    .andExpect(jsonPath("$.data.content[1].amount").value(20000))
                    .andExpect(jsonPath("$.data.content[1].status").value("DEBIT"));

            verify(sellerBalanceService, times(1))
                    .getBalanceHistory(eq(memberId), any(Pageable.class));
        }

        @Test
        @DisplayName("빈 내역도 정상적으로 조회됩니다.")
        void getBalanceHistory_empty() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            PageResponse<SellerBalanceHistoryInfo> emptyPage = PageResponse.from(
                    new PageImpl<>(List.of(), PageRequest.of(0, 20), 0)
            );

            when(sellerBalanceService.getBalanceHistory(eq(memberId), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // when & then
            mockMvc.perform(
                            get("/api/balances/history")
                                    .header(HeaderName.ID, memberId.toString())
                                    .param("page", "0")
                                    .param("size", "20")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0));

            verify(sellerBalanceService, times(1))
                    .getBalanceHistory(eq(memberId), any(Pageable.class));
        }

        @Test
        @DisplayName("두 번째 페이지를 조회합니다.")
        void getBalanceHistory_secondPage() throws Exception {
            // given
            UUID memberId = UUID.randomUUID();
            List<SellerBalanceHistoryInfo> histories = List.of(
                    new SellerBalanceHistoryInfo(
                            UUID.randomUUID(),
                            memberId,
                            UUID.randomUUID(),
                            30000L,
                            SellerBalanceHistoryStatus.CREDIT,
                            OffsetDateTime.now()
                    )
            );

            PageResponse<SellerBalanceHistoryInfo> pageResponse = PageResponse.from(
                    new PageImpl<>(histories, PageRequest.of(1, 10), 15)
            );

            when(sellerBalanceService.getBalanceHistory(eq(memberId), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(
                            get("/api/balances/history")
                                    .header(HeaderName.ID, memberId.toString())
                                    .param("page", "1")
                                    .param("size", "10")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.numberOfElements").value(1))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalPages").value(2));

            verify(sellerBalanceService, times(1))
                    .getBalanceHistory(eq(memberId), any(Pageable.class));
        }
    }
}
