package store._0982.point.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.BaseExceptionHandler;
import store._0982.common.exception.CustomException;
import store._0982.common.exception.DefaultErrorCode;
import store._0982.common.log.LogFormat;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(PaymentClientException.class)
    public ResponseEntity<ResponseDto<Void>> handlePaymentClientException(PaymentClientException e) {
        HttpStatus httpStatus = e.getStatus();
        log.error(LogFormat.errorOf(httpStatus, e.getMessage()), e);
        return ResponseEntity.status(httpStatus)
                .body(new ResponseDto<>(httpStatus, null, e.getMessage()));
    }

    // @Valid 어노테이션에 의한 검증 에러 핸들러
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<String>> handleInvalidArgumentException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return handleCustomException(new CustomException(DefaultErrorCode.INVALID_PARAMETER));
        }

        CustomException ex = switch (fieldError.getField()) {
            case "amount" -> new CustomException(CustomErrorCode.INVALID_AMOUNT);
            case "orderId" -> new CustomException(CustomErrorCode.ORDER_ID_IS_NULL);
            case "paymentKey" -> new CustomException(CustomErrorCode.PAYMENT_KEY_IS_NULL);
            case "idempotencyKey" -> new CustomException(CustomErrorCode.IDEMPOTENCY_KEY_IS_NULL);
            default -> new CustomException(DefaultErrorCode.INVALID_PARAMETER);
        };
        return handleCustomException(ex);
    }
}
