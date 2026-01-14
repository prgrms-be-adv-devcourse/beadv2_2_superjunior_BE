package store._0982.point.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import store._0982.point.client.dto.TossPaymentCancelRequest;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentClientTest {

    private TossPaymentClient tossPaymentClient;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        TossPaymentProperties properties = new TossPaymentProperties();
        properties.setSecretKey("test_secret_key");

        tossPaymentClient = new TossPaymentClient(restTemplate, properties);
    }

    @Test
    @DisplayName("결제 승인 API 호출이 성공한다")
    void confirm_success() throws Exception {
        // given
        String paymentKey = "test_payment_key";
        UUID orderId = UUID.randomUUID();
        int amount = 10000;

        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                orderId,
                amount,
                paymentKey
        );

        TossPaymentResponse expectedResponse = new TossPaymentResponse(
                paymentKey,
                orderId,
                amount,
                "CARD",
                "DONE",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                null
        );

        mockServer.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", org.hamcrest.Matchers.startsWith("Basic ")))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.amount").value(amount))
                .andExpect(jsonPath("$.paymentKey").value(paymentKey))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        // when
        TossPaymentResponse response = tossPaymentClient.confirm(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.paymentKey()).isEqualTo(paymentKey);
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.amount()).isEqualTo(amount);
        mockServer.verify();
    }

    @Test
    @DisplayName("결제 승인 API 실패 시 PaymentClientException이 발생한다")
    void confirm_fail() {
        // given
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                UUID.randomUUID(),
                10000,
                "test_payment_key"
        );

        String errorResponse = """
                {
                    "code": "INVALID_CARD_NUMBER",
                    "message": "유효하지 않은 카드 번호입니다"
                }
                """;

        mockServer.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse));

        // when & then
        assertThatThrownBy(() -> tossPaymentClient.confirm(request))
                .isInstanceOf(HttpStatusCodeException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("결제 취소 API 호출이 성공한다")
    void cancel_success() throws Exception {
        // given
        String paymentKey = "test_payment_key";
        int amount = 10000;
        String reason = "고객 요청";

        TossPaymentCancelRequest request = new TossPaymentCancelRequest(
                paymentKey,
                amount,
                reason
        );

        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                amount,
                reason,
                OffsetDateTime.now()
        );

        TossPaymentResponse expectedResponse = new TossPaymentResponse(
                paymentKey,
                UUID.randomUUID(),
                amount,
                "CARD",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of(cancelInfo)
        );

        mockServer.expect(requestTo("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", org.hamcrest.Matchers.startsWith("Basic ")))
                .andExpect(header("Idempotency-Key", org.hamcrest.Matchers.notNullValue()))
                .andExpect(jsonPath("$.cancelReason").value(reason))
                .andExpect(jsonPath("$.cancelAmount").value(amount))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        // when
        TossPaymentResponse response = tossPaymentClient.cancel(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.paymentKey()).isEqualTo(paymentKey);
        assertThat(response.cancels()).hasSize(1);
        assertThat(response.cancels().get(0).cancelAmount()).isEqualTo(amount);
        mockServer.verify();
    }

    @Test
    @DisplayName("결제 취소 API 실패 시 PaymentClientException이 발생한다")
    void cancel_fail() {
        // given
        TossPaymentCancelRequest request = new TossPaymentCancelRequest(
                "test_payment_key",
                10000,
                "고객 요청"
        );

        String errorResponse = """
                {
                    "code": "ALREADY_CANCELED_PAYMENT",
                    "message": "이미 취소된 결제입니다"
                }
                """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.startsWith("https://api.tosspayments.com/v1/payments/")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorResponse));

        // when & then
        assertThatThrownBy(() -> tossPaymentClient.cancel(request))
                .isInstanceOf(HttpStatusCodeException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("동일한 취소 요청에 대해 동일한 멱등키가 생성된다")
    void cancel_idempotencyKey() {
        // given
        String paymentKey = "test_payment_key";
        int amount = 10000;
        String reason = "고객 요청";

        TossPaymentCancelRequest request1 = new TossPaymentCancelRequest(paymentKey, amount, reason);
        TossPaymentCancelRequest request2 = new TossPaymentCancelRequest(paymentKey, amount, reason);

        // when
        String idempotencyKey1 = org.apache.commons.codec.digest.DigestUtils.sha256Hex(
                request1.paymentKey() + request1.amount() + request1.reason()
        );
        String idempotencyKey2 = org.apache.commons.codec.digest.DigestUtils.sha256Hex(
                request2.paymentKey() + request2.amount() + request2.reason()
        );

        // then
        assertThat(idempotencyKey1).isEqualTo(idempotencyKey2);
    }
}
