package store._0982.commerce.presentation.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.commerce.application.order.OrderService;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderCronController.class)
class OrderCronControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        reset(orderService);
    }

    @Test
    @DisplayName("주문 취소 재처리를 호출한다.")
    void retryCancelOrder_success() throws Exception {
        mockMvc.perform(post("/api/orders/cancel/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("주문 취소 재처리를 실행했습니다."));

        verify(orderService, times(1)).retryCancelOrder();
    }

    @Test
    @DisplayName("주문 취소 자동 승인 배치를 호출한다.")
    void autoCancelOrder_success() throws Exception {
        mockMvc.perform(post("/api/orders/cancel/auto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("주문 취소 재처리를 실행했습니다."));

        verify(orderService, times(1)).autoCancelOrder();
    }
}
