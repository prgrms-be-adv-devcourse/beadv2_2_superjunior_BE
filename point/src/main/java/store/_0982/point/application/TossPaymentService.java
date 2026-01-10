package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import store._0982.common.exception.CustomException;
import store._0982.common.log.LogFormat;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.TossPaymentClient;
import store._0982.point.client.dto.TossPaymentCancelRequest;
import store._0982.point.client.dto.TossPaymentConfirmRequest;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;
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
    public TossPaymentResponse confirmPayment(Payment payment, PaymentConfirmCommand command) {
        TossPaymentConfirmRequest request = TossPaymentConfirmRequest.from(command);
        TossPaymentResponse tossPaymentResponse = executeWithExceptionHandling(() -> tossPaymentClient.confirm(request));

        if (!payment.getOrderId().equals(tossPaymentResponse.orderId())) {
            throw new CustomException(CustomErrorCode.ORDER_ID_MISMATCH);
        }
        return tossPaymentResponse;
    }

    @ServiceLog
    public TossPaymentResponse cancelPayment(Payment payment, PointRefundCommand command) {
        TossPaymentCancelRequest request = TossPaymentCancelRequest.from(payment, command);
        return executeWithExceptionHandling(() -> tossPaymentClient.cancel(request));
    }

    /**
     * 토스 API 호출 시 발생하는 예외를 통합 처리합니다.
     *
     * @param apiCall 토스 API 호출 로직
     * @return API 응답
     */
    private static TossPaymentResponse executeWithExceptionHandling(Supplier<TossPaymentResponse> apiCall) {
        try {
            return apiCall.get();
        } catch (CustomException | PaymentClientException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new CustomException(CustomErrorCode.PAYMENT_API_TIMEOUT);
        } catch (Exception e) {
            log.error(LogFormat.errorOf(HttpStatus.BAD_GATEWAY, e.getMessage()), e);
            throw new CustomException(CustomErrorCode.PAYMENT_API_ERROR);
        }
    }
}
