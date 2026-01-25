package store._0982.point.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.point.application.webhook.TossWebhookFacade;
import store._0982.point.common.WebhookHeaders;
import store._0982.point.presentation.dto.TossWebhookRequest;

import java.time.OffsetDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")
public class WebhookController {

    private final TossWebhookFacade tossWebhookFacade;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/toss")
    public ResponseDto<Void> handleTossWebhook(
            @RequestHeader(WebhookHeaders.TOSS_RETRY_COUNT) int retryCount,
            @RequestHeader(WebhookHeaders.TOSS_WEBHOOK_ID) String webhookId,
            @RequestHeader(WebhookHeaders.TOSS_TRANSMISSION_TIME) OffsetDateTime transmissionTime,
            @RequestBody @Valid TossWebhookRequest request
    ) throws JsonProcessingException {
        tossWebhookFacade.handleTossWebhook(retryCount, webhookId, transmissionTime, request);
        return new ResponseDto<>(HttpStatus.OK, null, "웹훅 이벤트 처리 성공");
    }
}
