package store._0982.member.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.BaseExceptionHandler;
import store._0982.common.exception.CustomException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    // @Valid 어노테이션에 의한 검증 에러 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<String>> handleInvalidArgumentException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null) {
            return handleCustomException(new CustomException(CustomErrorCode.INVALID_PARAMETER));
        }

        CustomException ex = switch (fieldError.getField()) {
            case "email" -> new CustomException(CustomErrorCode.INVALID_EMAIL);
            case "password" -> new CustomException(CustomErrorCode.INVALID_PASSWORD);
            case "name" -> new CustomException(CustomErrorCode.INVALID_NAME);
            case "phoneNumber" -> new  CustomException(CustomErrorCode.INVALID_PHONE_NUMBER);
            case "accountNumber" -> new CustomException(CustomErrorCode.INVALID_SELLER_ACCOUNT_NUMBER);
            case "bankCode" -> new CustomException(CustomErrorCode.INVALID_SELLER_BANK_CODE);
            case "accountHolder" -> new CustomException(CustomErrorCode.INVALID_SELLER_ACCOUNT_HOLDER);
            case "businessRegistrationNumber" -> new CustomException(CustomErrorCode.INVALID_SELLER_BUSINESS_REGISTRATION_NUMBER);
            case "address" -> new CustomException(CustomErrorCode.INVALID_ADDRESS);
            case "addressDetail" -> new CustomException(CustomErrorCode.INVALID_ADDRESS_DETAIL);
            case "postalCode" -> new CustomException(CustomErrorCode.INVALID_POSTAL_CODE);
            case "receiverName" -> new CustomException(CustomErrorCode.INVALID_RECEIVER_NAME);
            default -> new CustomException(CustomErrorCode.INVALID_PARAMETER);
        };
        return handleCustomException(ex);
    }

}
