package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.exception.NegligibleWebhookErrorType;
import store._0982.point.exception.NegligibleWebhookException;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TossWebhookFacadeTest {

    @Mock
    private TossWebhookService tossWebhookService;

    @Mock
    private WebhookLogService webhookLogService;

    @InjectMocks
    private TossWebhookFacade tossWebhookFacade;

    private String webhookId;
    private OffsetDateTime transmissionTime;
    private TossWebhookRequest request;
    private WebhookLog webhookLog;

    @BeforeEach
    void setUp() {
        webhookId = UUID.randomUUID().toString();
        transmissionTime = OffsetDateTime.now();

        TossPaymentInfo paymentInfo = TossPaymentInfo.builder()
                .paymentKey("test_payment_key")
                .orderId(UUID.randomUUID())
                .amount(10000)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        request = new TossWebhookRequest(
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                LocalDateTime.now(),
                paymentInfo
        );

        webhookLog = mock(WebhookLog.class);
    }

    @Test
    @DisplayName("유효한 웹훅 이벤트를 성공적으로 처리한다")
    void handleTossWebhook_success() throws JsonProcessingException {
        // given
        when(webhookLogService.getUnfinishedWebhook(anyString(), any(), any(), anyInt()))
                .thenReturn(Optional.of(webhookLog));
        doNothing().when(tossWebhookService).processWebhookPayment(any());

        // when
        tossWebhookFacade.handleTossWebhook(1, webhookId, transmissionTime, request);

        // then
        verify(webhookLogService).getUnfinishedWebhook(webhookId, transmissionTime, request, 1);
        verify(tossWebhookService).processWebhookPayment(request.data());
        verify(webhookLogService).markCompleted(any(WebhookLog.class));
        verify(webhookLogService, never()).markFailed(any(WebhookLog.class), anyString());
    }

    @Test
    @DisplayName("유효하지 않은 이벤트 타입은 NegligibleWebhookException을 발생시킨다")
    void handleTossWebhook_invalidEventType() throws JsonProcessingException {
        // given
        TossPaymentInfo paymentInfo = TossPaymentInfo.builder()
                .paymentKey("test_payment_key")
                .orderId(UUID.randomUUID())
                .amount(10000)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();

        TossWebhookRequest invalidRequest = new TossWebhookRequest(
                "INVALID_EVENT_TYPE",
                LocalDateTime.now(),
                paymentInfo
        );

        // when & then
        assertThatThrownBy(() -> tossWebhookFacade.handleTossWebhook(1, webhookId, transmissionTime, invalidRequest))
                .isInstanceOf(NegligibleWebhookException.class)
                .hasMessageContaining(NegligibleWebhookErrorType.INVALID_EVENT_TYPE.getMessage());

        verify(webhookLogService, never()).getUnfinishedWebhook(anyString(), any(), any(), anyInt());
        verify(tossWebhookService, never()).processWebhookPayment(any());
    }

    @Test
    @DisplayName("이미 처리된 웹훅은 조용히 무시한다")
    void handleTossWebhook_alreadyProcessed() throws JsonProcessingException {
        // given
        when(webhookLogService.getUnfinishedWebhook(anyString(), any(), any(), anyInt()))
                .thenReturn(Optional.empty());

        // when
        tossWebhookFacade.handleTossWebhook(1, webhookId, transmissionTime, request);

        // then
        verify(webhookLogService).getUnfinishedWebhook(webhookId, transmissionTime, request, 1);
        verify(tossWebhookService, never()).processWebhookPayment(any());
    }

    @Test
    @DisplayName("웹훅 처리 실패 시 로그를 실패 상태로 변경하고 예외를 재발생시킨다")
    void handleTossWebhook_processFailed() throws JsonProcessingException {
        // given
        String errorMessage = "결제 처리 실패";
        RuntimeException exception = new RuntimeException(errorMessage);

        when(webhookLogService.getUnfinishedWebhook(anyString(), any(), any(), anyInt()))
                .thenReturn(Optional.of(webhookLog));
        doThrow(exception).when(tossWebhookService).processWebhookPayment(any());

        // when & then
        assertThatThrownBy(() -> tossWebhookFacade.handleTossWebhook(1, webhookId, transmissionTime, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(errorMessage);

        verify(webhookLogService).getUnfinishedWebhook(webhookId, transmissionTime, request, 1);
        verify(tossWebhookService).processWebhookPayment(request.data());
        verify(webhookLogService).markFailed(any(WebhookLog.class), eq(errorMessage));
        verify(webhookLogService, never()).markCompleted(any(WebhookLog.class));
    }
}
