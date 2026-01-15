package store._0982.point.application;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import store._0982.common.exception.CustomException;
import store._0982.common.log.LogFormat;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.application.dto.PgConfirmCommand;
import store._0982.point.client.TossPaymentClient;
import store._0982.point.client.dto.TossPaymentCancelRequest;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.exception.CustomErrorCode;
import store._0982.point.exception.PaymentClientException;

import java.util.function.Supplier;

/**
 * 토스 API를 호출해 주는 서비스입니다.
 * <p>{@link TossPaymentClient} 대신 이 서비스를 이용해 주세요.</p>
 *
 * @author Minhyung Kim
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {
    private final TossPaymentClient tossPaymentClient;

    @ServiceLog
    @RateLimiter(name = "pg-confirm")
    public TossPaymentInfo confirmPayment(PgPayment pgPayment, PgConfirmCommand command) {
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.from(command);
        TossPaymentInfo tossPaymentInfo = executeWithExceptionHandling(() -> tossPaymentClient.confirm(request));

        if (!pgPayment.getOrderId().equals(tossPaymentInfo.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH);
        }
        return tossPaymentInfo;
    }

    @ServiceLog
    @RateLimiter(name = "pg-cancel")
    public TossPaymentInfo cancelPayment(PgPayment pgPayment, PgCancelCommand command) {
        TossPaymentCancelRequest request = TossPaymentCancelRequest.from(pgPayment, command);
        return executeWithExceptionHandling(() -> tossPaymentClient.cancel(request));
    }

    /**
     * 토스 API 호출 시 발생하는 예외를 통합 처리합니다.
     *
     * @param apiCall 토스 API 호출 로직
     * @return API 응답
     */
    private static TossPaymentInfo executeWithExceptionHandling(Supplier<TossPaymentInfo> apiCall) {
        try {
            return apiCall.get();
        } catch (CustomException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            HttpStatus httpStatus = HttpStatus.resolve(e.getStatusCode().value());
            TossPaymentErrorResponse response = e.getResponseBodyAs(TossPaymentErrorResponse.class);
            if (response == null) {
                throw new CustomException(CustomErrorCode.PAYMENT_API_ERROR);
            }
            throw new PaymentClientException(httpStatus, response.code(), response.message());
        } catch (ResourceAccessException e) {
            throw new CustomException(CustomErrorCode.PAYMENT_API_TIMEOUT);
        } catch (Exception e) {
            log.error(LogFormat.errorOf(HttpStatus.BAD_GATEWAY, e.getMessage()), e);
            throw new CustomException(CustomErrorCode.PAYMENT_API_ERROR);
        }
    }

    private record TossPaymentErrorResponse(String code, String message) {
    }
}
