package store._0982.commerce.integration.sellerbalance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.sellerbalance.SellerBalance;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistory;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryStatus;
import store._0982.commerce.domain.sellerbalance.SellerBalanceRepository;
import store._0982.commerce.fixture.SellerBalanceFixture;
import store._0982.commerce.fixture.SellerBalanceHistoryFixture;
import store._0982.common.HeaderName;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("SellerBalance 통합 테스트")
class SellerBalanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SellerBalanceRepository sellerBalanceRepository;

    @Autowired
    private SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    private UUID testMemberId;
    private SellerBalance testSellerBalance;

    @BeforeEach
    void setUp() {
        testMemberId = UUID.randomUUID();
        testSellerBalance = SellerBalanceFixture.create(testMemberId);
        sellerBalanceRepository.save(testSellerBalance);
    }

    @Test
    @DisplayName("판매자 balance를 조회합니다.")
    void getBalance_success() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/balances")
                                .header(HeaderName.ID, testMemberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("조회되었습니다."))
                .andExpect(jsonPath("$.data.sellerBalanceId").value(testSellerBalance.getBalanceId().toString()))
                .andExpect(jsonPath("$.data.memberId").value(testMemberId.toString()))
                .andExpect(jsonPath("$.data.balance").value(0L));
    }

    @Test
    @DisplayName("balance 변동 내역을 페이징하여 조회합니다.")
    void getBalanceHistory_success() throws Exception {
        // given
        UUID settlementId1 = UUID.randomUUID();
        UUID settlementId2 = UUID.randomUUID();
        UUID settlementId3 = UUID.randomUUID();

        SellerBalanceHistory history1 = SellerBalanceHistoryFixture.create(
                testMemberId,
                settlementId1,
                50000L,
                SellerBalanceHistoryStatus.CREDIT
        );

        SellerBalanceHistory history2 = SellerBalanceHistoryFixture.create(
                testMemberId,
                settlementId2,
                20000L,
                SellerBalanceHistoryStatus.DEBIT
        );

        SellerBalanceHistory history3 = SellerBalanceHistoryFixture.create(
                testMemberId,
                settlementId3,
                30000L,
                SellerBalanceHistoryStatus.CREDIT
        );

        sellerBalanceHistoryRepository.save(history1);
        sellerBalanceHistoryRepository.save(history2);
        sellerBalanceHistoryRepository.save(history3);

        // when & then
        mockMvc.perform(
                        get("/api/balances/history")
                                .header(HeaderName.ID, testMemberId.toString())
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("조회되었습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.numberOfElements").value(3));
    }

    @Test
    @DisplayName("balance 변동 내역이 없어도 빈 페이지를 정상 반환합니다.")
    void getBalanceHistory_empty() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/balances/history")
                                .header(HeaderName.ID, testMemberId.toString())
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    @DisplayName("페이징 파라미터를 다르게 설정하여 조회합니다.")
    void getBalanceHistory_withPaging() throws Exception {
        // given
        for (int i = 0; i < 5; i++) {
            SellerBalanceHistory history = SellerBalanceHistoryFixture.create(
                    testMemberId,
                    UUID.randomUUID(),
                    (i + 1) * 10000L,
                    i % 2 == 0 ? SellerBalanceHistoryStatus.CREDIT : SellerBalanceHistoryStatus.DEBIT
            );
            sellerBalanceHistoryRepository.save(history);
        }

        // when & then
        mockMvc.perform(
                        get("/api/balances/history")
                                .header(HeaderName.ID, testMemberId.toString())
                                .param("page", "0")
                                .param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.numberOfElements").value(2))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));

        // when & then
        mockMvc.perform(
                        get("/api/balances/history")
                                .header(HeaderName.ID, testMemberId.toString())
                                .param("page", "1")
                                .param("size", "2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.numberOfElements").value(2))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(false));
    }

    @Test
    @DisplayName("다른 판매자의 balance는 조회되지 않습니다.")
    void getBalanceHistory_onlyOwnHistory() throws Exception {
        // given
        UUID otherMemberId = UUID.randomUUID();
        SellerBalance otherSellerBalance = SellerBalanceFixture.create(otherMemberId);
        sellerBalanceRepository.save(otherSellerBalance);

        SellerBalanceHistory otherHistory = SellerBalanceHistoryFixture.create(
                otherMemberId,
                UUID.randomUUID(),
                100000L,
                SellerBalanceHistoryStatus.CREDIT
        );
        sellerBalanceHistoryRepository.save(otherHistory);

        SellerBalanceHistory myHistory = SellerBalanceHistoryFixture.create(
                testMemberId,
                UUID.randomUUID(),
                50000L,
                SellerBalanceHistoryStatus.CREDIT
        );
        sellerBalanceHistoryRepository.save(myHistory);

        // when & then
        mockMvc.perform(
                        get("/api/balances/history")
                                .header(HeaderName.ID, testMemberId.toString())
                                .param("page", "0")
                                .param("size", "20")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].memberId").value(testMemberId.toString()))
                .andExpect(jsonPath("$.data.content[0].amount").value(50000));
    }

    @Test
    @DisplayName("memberId 헤더가 없으면 401 에러가 발생합니다.")
    void getBalance_missingMemberId() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/balances")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인 정보가 없습니다."));
    }

    @Test
    @DisplayName("잘못된 UUID 형식이면 400 에러가 발생합니다.")
    void getBalance_invalidUUID() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/api/balances")
                                .header(HeaderName.ID, "uuid")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("적절하지 않은 요청 값이 존재합니다."));
    }
}
