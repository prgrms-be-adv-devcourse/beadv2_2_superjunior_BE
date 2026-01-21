package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.constant.WebhookStatus;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.repository.WebhookLogRepository;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookLogServiceTest {

    @Mock
    private WebhookLogRepository webhookLogRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebhookLogService webhookLogService;

    private String webhookId;
    private OffsetDateTime transmissionTime;
    private TossWebhookRequest request;

    @BeforeEach
    void setUp() throws JsonProcessingException {
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

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"eventType\":\"PAYMENT_STATUS_CHANGED\"}");
    }

    @Test
    @DisplayName("새로운 웹훅 로그를 생성한다")
    void getUnfinishedWebhook_create_new() throws JsonProcessingException {
        // given
        when(webhookLogRepository.findByWebhookIdWithLock(webhookId)).thenReturn(Optional.empty());
        when(webhookLogRepository.save(any(WebhookLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Optional<WebhookLog> result = webhookLogService.getUnfinishedWebhook(webhookId, transmissionTime, request, 1);

        // then
        assertThat(result).isPresent();
        verify(webhookLogRepository).findByWebhookIdWithLock(webhookId);
        verify(webhookLogRepository).save(any(WebhookLog.class));
        verify(objectMapper, times(2)).writeValueAsString(any());
    }

    @Test
    @DisplayName("기존 웹훅 로그를 업데이트한다")
    void getUnfinishedWebhook_update_existing() throws JsonProcessingException {
        // given
        WebhookLog existingLog = WebhookLog.create(
                webhookId,
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                "{\"test\":\"data\"}",
                OffsetDateTime.now(),
                OffsetDateTime.now().minusMinutes(1),
                1
        );

        when(webhookLogRepository.findByWebhookIdWithLock(webhookId)).thenReturn(Optional.of(existingLog));

        // when
        Optional<WebhookLog> result = webhookLogService.getUnfinishedWebhook(webhookId, transmissionTime, request, 2);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(existingLog);
        verify(webhookLogRepository).findByWebhookIdWithLock(webhookId);
        verify(webhookLogRepository, never()).save(any(WebhookLog.class));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("이미 처리된 웹훅은 빈 Optional을 반환한다")
    void getUnfinishedWebhook_already_processed() throws JsonProcessingException {
        // given
        WebhookLog processedLog = WebhookLog.create(
                webhookId,
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                "{\"test\":\"data\"}",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                1
        );
        processedLog.markSuccess();

        when(webhookLogRepository.findByWebhookIdWithLock(webhookId)).thenReturn(Optional.of(processedLog));

        // when
        Optional<WebhookLog> result = webhookLogService.getUnfinishedWebhook(webhookId, transmissionTime, request, 1);

        // then
        assertThat(result).isEmpty();
        verify(webhookLogRepository).findByWebhookIdWithLock(webhookId);
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("재시도 카운트가 증가하여 업데이트된다")
    void getUnfinishedWebhook_retry_count_increases() throws JsonProcessingException {
        // given
        WebhookLog existingLog = WebhookLog.create(
                webhookId,
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                "{\"test\":\"data\"}",
                OffsetDateTime.now(),
                OffsetDateTime.now().minusMinutes(1),
                1
        );

        when(webhookLogRepository.findByWebhookIdWithLock(webhookId)).thenReturn(Optional.of(existingLog));

        // when
        webhookLogService.getUnfinishedWebhook(webhookId, transmissionTime, request, 3);

        // then
        verify(webhookLogRepository).findByWebhookIdWithLock(webhookId);
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    @DisplayName("pessimistic lock을 사용하여 웹훅 로그를 조회한다")
    void getUnfinishedWebhook_with_lock() throws JsonProcessingException {
        // given
        when(webhookLogRepository.findByWebhookIdWithLock(webhookId)).thenReturn(Optional.empty());
        when(webhookLogRepository.save(any(WebhookLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        webhookLogService.getUnfinishedWebhook(webhookId, transmissionTime, request, 1);

        // then
        verify(webhookLogRepository).findByWebhookIdWithLock(webhookId);
    }
}
