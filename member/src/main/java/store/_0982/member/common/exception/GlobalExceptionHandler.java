package store._0982.member.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import store._0982.member.common.HeaderName;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.common.log.LogFormat;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<String>> handleCustomException(CustomException e) {
        CustomErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();
        log.error(LogFormat.errorOf(httpStatus, e.getMessage()), e);
        return ResponseEntity.status(httpStatus)
                .body(new ResponseDto<>(httpStatus, null, e.getMessage()));
    }

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
            case "accountNumber" -> new CustomException(CustomErrorCode.INVALID_SELLER_ACCOUNT_NUMBER);
            case "bankCode" -> new CustomException(CustomErrorCode.INVALID_SELLER_BANK_CODE);
            case "accountHolder" -> new CustomException(CustomErrorCode.INVALID_SELLER_ACCOUNT_HOLDER);
            case "businessRegistrationNumber" -> new CustomException(CustomErrorCode.INVALID_SELLER_BUSINESS_REGISTRATION_NUMBER);
            default -> new CustomException(CustomErrorCode.INVALID_PARAMETER);
        };
        return handleCustomException(ex);
    }

    // @RequestHeader 어노테이션에 의한 검증 에러 핸들러
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseDto<String>> handleMissingHeaderException(MissingRequestHeaderException e) {
        CustomException ex = switch (e.getHeaderName()) {
            case HeaderName.ID -> new CustomException(CustomErrorCode.NO_LOGIN_INFO);
            case HeaderName.EMAIL -> new CustomException(CustomErrorCode.NO_EMAIL_INFO);
            case HeaderName.ROLE -> new CustomException(CustomErrorCode.NO_ROLE_INFO);
            default -> new CustomException(CustomErrorCode.REQUEST_HEADER_IS_NULL);
        };
        return handleCustomException(ex);
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDto<String> handleSecurityException(SecurityException e) {
        log.error(LogFormat.errorOf(HttpStatus.FORBIDDEN, e.getMessage()), e);
        return new ResponseDto<>(HttpStatus.FORBIDDEN, null, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error(LogFormat.errorOf(HttpStatus.NOT_FOUND, e.getMessage()), e);
        return new ResponseDto<>(HttpStatus.NOT_FOUND, null, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<String> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error(LogFormat.errorOf(HttpStatus.NOT_FOUND, e.getMessage()), e);
        return new ResponseDto<>(HttpStatus.NOT_FOUND, null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<String> handleException(Exception e) {
        log.error(LogFormat.errorOf(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), e);
        return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
    }
}
