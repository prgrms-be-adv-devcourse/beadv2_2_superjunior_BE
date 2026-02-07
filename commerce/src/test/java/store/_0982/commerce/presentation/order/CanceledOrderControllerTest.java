package store._0982.commerce.presentation.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.presentation.order.dto.OrderCancelRequest;
import store._0982.common.HeaderName;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.dto.PageResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class CanceledOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        reset(orderService);
    }

    @Test
    @DisplayName("주문을 취소한다.")
    void cancelOrder_success() throws Exception {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCancelRequest request = new OrderCancelRequest(
                CancelReason.CHANGE_OF_MIND,
                "단순 변심",
                "idem-key-123"
        );

        // when & then
        mockMvc.perform(
                        post("/api/orders/cancel/{orderId}", orderId)
                                .header(HeaderName.ID, memberId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("주문 취소 되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(orderService, times(1))
                .cancelOrder(any());
    }

    @Test
    @DisplayName("memberId 헤더가 없으면 주문 취소에 실패한다.")
    void cancelOrder_missingMemberId() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderCancelRequest request = new OrderCancelRequest(
                CancelReason.CHANGE_OF_MIND,
                "사유",
                "idem"
        );

        mockMvc.perform(
                        post("/api/orders/cancel/{orderId}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("로그인 정보가 없습니다."));

        verify(orderService, times(0)).cancelOrder(any());
    }

    @Test
    @DisplayName("잘못된 요청 본문이면 주문 취소에 실패한다.")
    void cancelOrder_invalidRequest() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest(
                CancelReason.CHANGE_OF_MIND,
                "",
                "idem"
        );

        mockMvc.perform(
                        post("/api/orders/cancel/{orderId}",  "invalid")
                                .header(HeaderName.ID, UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("적절하지 않은 요청 값이 존재합니다."));

        verify(orderService, times(0)).cancelOrder(any());
    }

    @Test
    @DisplayName("주문 취소 내역을 조회한다.")
    void getCanceledOrders_success() throws Exception {
        UUID memberId = UUID.randomUUID();
        PageResponse<OrderCancelInfo> response = new PageResponse<>(
                List.of(
                        new OrderCancelInfo(
                                UUID.randomUUID(),
                                CancelStatus.REQUESTED,
                                10_000L,
                                1_000L,
                                0L,
                                9_000L,
                                CancelReason.CHANGE_OF_MIND,
                                "사유",
                                OffsetDateTime.now()
                        )
                ),
                1,
                1L,
                true,
                true,
                20,
                1
        );
        when(orderService.getCanceledOrders(eq(memberId), any()))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/orders/cancel")
                                .header(HeaderName.ID, memberId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("주문 취소 목록을 조회했습니다."))
                .andExpect(jsonPath("$.data.content[0].orderId").exists());

        verify(orderService, times(1)).getCanceledOrders(eq(memberId), any());
    }
}
