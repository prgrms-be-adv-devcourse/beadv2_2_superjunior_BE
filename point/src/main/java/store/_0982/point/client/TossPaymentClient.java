package store._0982.point.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import store._0982.point.client.dto.TossPaymentCancelRequest;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 실제로 토스 API를 호출하는 클래스입니다.
 * <p>API 호출 메서드를 정의할 때는 client.dto 패키지에서 정의된 클래스만 파라미터로 이용해 주세요.</p>
 *
 * @author Minhyung Kim
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final RestTemplate restTemplate;
    private final TossPaymentProperties properties;

    public TossPaymentResponse confirm(TossPaymentConfirmRequest request) {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Toss secret key is not configured");
        }
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.paymentKey());
        body.put("orderId", request.orderId());
        body.put("amount", request.amount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            TossPaymentResponse tossPaymentResponse = restTemplate.postForObject(CONFIRM_URL, entity, TossPaymentResponse.class);
            log.debug(String.valueOf(tossPaymentResponse));
            return tossPaymentResponse;
        } catch (HttpStatusCodeException ex) {
            HttpStatusCode statusCode = ex.getStatusCode();
            String responseBody = ex.getResponseBodyAsString();
            throw new IllegalStateException("Toss confirm failed (" + statusCode + "): " + responseBody, ex);
        }
    }

    public TossPaymentResponse cancel(TossPaymentCancelRequest request) {
        String url = "https://api.tosspayments.com/v1/payments/" + request.paymentKey() + "/cancel";

        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Toss secret key is not configured");
        }
        HttpHeaders headers = createHeaders();

        //멱등키
        headers.set("Idempotency-Key", UUID.randomUUID().toString());

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.reason());
        body.put("cancelAmount", request.amount());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, entity, TossPaymentResponse.class);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = properties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }
}
