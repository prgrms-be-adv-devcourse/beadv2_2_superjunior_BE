package store._0982.point.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import store._0982.point.client.dto.TossPaymentCancelRequest;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentInfo;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

    private static final String BASE_URL = "https://api.tosspayments.com/v1/payments";

    private final RestTemplate restTemplate;
    private final TossPaymentProperties properties;

    @Retry(name = "pg-api")
    @CircuitBreaker(name = "pg-confirm")
    public TossPaymentInfo confirm(TossPaymentConfirmRequest request) {
        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.paymentKey());
        body.put("orderId", request.orderId());
        body.put("amount", request.amount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return handlePaymentApiErrorAndGet(
                () -> restTemplate.postForObject(BASE_URL + "/confirm", entity, TossPaymentInfo.class));
    }

    @Retry(name = "pg-api")
    public TossPaymentInfo cancel(TossPaymentCancelRequest request) {
        HttpHeaders headers = createHeaders();

        // paymentKey, amount, reason을 조합해 해시값을 생성 후 멱등키로 이용
        // 기본 sha256 메서드는 하나에 8비트로 변환되기 때문에 byte[]로 반환되고,
        // sha256Hex 메서드는 하나에 16진수(4비트)로 변환되기 때문에 문자열로 표현 가능하므로 String으로 반환됨
        String idempotencyKey = DigestUtils.sha256Hex(request.paymentKey() + request.amount() + request.reason());
        headers.set("Idempotency-Key", idempotencyKey);

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", request.reason());
        body.put("cancelAmount", request.amount());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        String url = BASE_URL + "/" + request.paymentKey() + "/cancel";
        return handlePaymentApiErrorAndGet(
                () -> restTemplate.postForObject(url, entity, TossPaymentInfo.class));
    }

    @Retry(name = "pg-api")
    public TossPaymentInfo getPaymentByKey(String paymentKey) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return handlePaymentApiErrorAndGet(
                () -> restTemplate.exchange(BASE_URL + "/" + paymentKey, HttpMethod.GET, entity, TossPaymentInfo.class).getBody());
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = properties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        return headers;
    }

    private TossPaymentInfo handlePaymentApiErrorAndGet(Supplier<TossPaymentInfo> apiCall) {
        try {
            TossPaymentInfo response = apiCall.get();
            log.debug(String.valueOf(response));
            return response;
        } catch (HttpStatusCodeException e) {
            log.debug(e.getResponseBodyAsString());
            throw e;
        }
    }
}
