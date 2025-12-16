package store._0982.order.exception;

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
    // @Valid 어노테이션에 의한 검증 에러 핸들러
    @Override
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<String>> handleInvalidArgumentException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return handleCustomException(new CustomException(DefaultErrorCode.INVALID_PARAMETER));
        }

        CustomException ex = switch (fieldError.getField()) {
            case "quantity" -> new CustomException(CustomErrorCode.INVALID_QUANTITY);
            case "price" -> new CustomException(CustomErrorCode.INVALID_PRICE);
            case "address", "addressDetail" -> new CustomException(CustomErrorCode.INVALID_ADDRESS);
            case "postalCode" -> new CustomException(CustomErrorCode.POSTAL_CODE_IS_NULL);
            case "receiverName" -> new CustomException(CustomErrorCode.INVALID_RECEIVER_NAME);
            case "sellerId" -> new CustomException(CustomErrorCode.SELLER_ID_IS_NULL);
            case "groupPurchaseId" -> new CustomException(CustomErrorCode.GROUP_PURCHASE_ID_IS_NULL);
            default -> new CustomException(DefaultErrorCode.INVALID_PARAMETER);
        };
        return handleCustomException(ex);
    }
}
