package store._0982.commerce.presentation.sellerbalance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceThumbnailInfo;
import store._0982.commerce.presentation.sellerbalance.dto.SellerBalanceRequest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalSellerBalanceController.class)
class InternalSellerBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SellerBalanceService sellerBalanceService;

    @BeforeEach
    void setUp() {
        reset(sellerBalanceService);
    }

    @Test
    @DisplayName("판매자 잔액을 생성한다.")
    void createSellerBalance_success() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        SellerBalanceRequest request = new SellerBalanceRequest(memberId);

        SellerBalanceThumbnailInfo info = new SellerBalanceThumbnailInfo(
                memberId
        );

        when(sellerBalanceService.createSellerBalance(any()))
                .thenReturn(info);

        // when & then
        mockMvc.perform(
                        post("/internal/balances")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("seller balance 생성되었습니다."))
                .andExpect(jsonPath("$.data.sellerId").value(memberId.toString()));

        verify(sellerBalanceService, times(1))
                .createSellerBalance(any());
    }
}
