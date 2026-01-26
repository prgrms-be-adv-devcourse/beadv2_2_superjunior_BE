package store._0982.point.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import store._0982.point.application.webhook.TossWebhookFacade;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.WebhookHeaders;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TossWebhookFacade tossWebhookFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TossWebhookFacade tossWebhookFacade() {
            return mock(TossWebhookFacade.class);
        }
    }

    @Test
    @DisplayName("토스 웹훅 이벤트를 정상 처리한다")
    void handleTossWebhook() throws Exception {
        // given
        String webhookId = "wh_test_123";
        int retryCount = 0;
        OffsetDateTime transmissionTime = OffsetDateTime.now();

        TossPaymentInfo paymentInfo = TossPaymentInfo.builder()
                .paymentKey("test_key")
                .status(TossPaymentInfo.Status.DONE)
                .build();

        TossWebhookRequest request = new TossWebhookRequest(
                "PAYMENT_STATUS_CHANGED",
                LocalDateTime.now(),
                paymentInfo
        );

        doNothing().when(tossWebhookFacade).handleTossWebhook(
                eq(retryCount),
                eq(webhookId),
                eq(transmissionTime),
                any(TossWebhookRequest.class)
        );

        // when & then
        mockMvc.perform(post("/webhooks/toss")
                        .header(WebhookHeaders.TOSS_RETRY_COUNT, retryCount)
                        .header(WebhookHeaders.TOSS_WEBHOOK_ID, webhookId)
                        .header(WebhookHeaders.TOSS_TRANSMISSION_TIME, transmissionTime.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(tossWebhookFacade).handleTossWebhook(
                eq(retryCount),
                eq(webhookId),
                eq(transmissionTime),
                any(TossWebhookRequest.class)
        );
    }

    @Test
    @DisplayName("토스 웹훅 재시도 요청을 처리한다")
    void handleTossWebhook_withRetry() throws Exception {
        // given
        String webhookId = "wh_test_456";
        int retryCount = 2;
        OffsetDateTime transmissionTime = OffsetDateTime.now();

        TossPaymentInfo paymentInfo = TossPaymentInfo.builder()
                .paymentKey("test_key_retry")
                .status(TossPaymentInfo.Status.CANCELED)
                .build();

        TossWebhookRequest request = new TossWebhookRequest(
                "PAYMENT_STATUS_CHANGED",
                LocalDateTime.now(),
                paymentInfo
        );

        doNothing().when(tossWebhookFacade).handleTossWebhook(
                eq(retryCount),
                eq(webhookId),
                eq(transmissionTime),
                any(TossWebhookRequest.class)
        );

        // when & then
        mockMvc.perform(post("/webhooks/toss")
                        .header(WebhookHeaders.TOSS_RETRY_COUNT, retryCount)
                        .header(WebhookHeaders.TOSS_WEBHOOK_ID, webhookId)
                        .header(WebhookHeaders.TOSS_TRANSMISSION_TIME, transmissionTime.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(tossWebhookFacade).handleTossWebhook(
                eq(retryCount),
                eq(webhookId),
                eq(transmissionTime),
                any(TossWebhookRequest.class)
        );
    }
}
