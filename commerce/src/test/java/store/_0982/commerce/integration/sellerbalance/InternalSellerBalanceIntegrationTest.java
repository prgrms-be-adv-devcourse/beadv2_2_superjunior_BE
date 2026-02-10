package store._0982.commerce.integration.sellerbalance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.sellerbalance.SellerBalanceRepository;
import store._0982.commerce.presentation.sellerbalance.dto.SellerBalanceRequest;
import store._0982.commerce.support.BaseIntegrationTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class InternalSellerBalanceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SellerBalanceRepository sellerBalanceRepository;

    @Test
    @DisplayName("내부 API를 통해 판매자 잔액 생성 시 DB에 저장된다")
    void createSellerBalance_success() throws Exception {
        UUID sellerId = UUID.randomUUID();
        SellerBalanceRequest request = new SellerBalanceRequest(sellerId);

        mockMvc.perform(
                        post("/internal/balances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("seller balance 생성되었습니다."))
                .andExpect(jsonPath("$.data.sellerId").value(sellerId.toString()));

        assertThat(sellerBalanceRepository.findByMemberId(sellerId)).isPresent();
    }

    @Test
    @DisplayName("내부 API에서 sellerId 누락 시 400을 반환한다")
    void createSellerBalance_missingSellerId() throws Exception {
        SellerBalanceRequest request = new SellerBalanceRequest(null);

        mockMvc.perform(
                        post("/internal/balances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("SellerId 값이 없습니다."));
    }
}
