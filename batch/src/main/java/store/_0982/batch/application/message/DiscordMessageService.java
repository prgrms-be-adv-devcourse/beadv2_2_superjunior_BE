package store._0982.batch.application.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@Service
public class DiscordMessageService {

    @Value("${logging.discord.webhook-url}")
    String discordWebhookUrl;

    public void sendDiscordWebhookMessage(DiscordWebhookMessage message) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");
            HttpEntity<DiscordWebhookMessage> messageEntity = new HttpEntity<>(message, httpHeaders);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.exchange(
                    discordWebhookUrl,
                    POST,
                    messageEntity,
                    String.class
            );
            if (response.getStatusCode().value() != NO_CONTENT.value()) {
                log.error("[ERROR] 디스코드 메시지 전송 이후 에러 발생");
            }
        } catch (Exception e) {
            log.error("[ERROR] 디스코드 메세지 에러 ", e);
        }
    }

    public record DiscordWebhookMessage(String content) {
    }
}
