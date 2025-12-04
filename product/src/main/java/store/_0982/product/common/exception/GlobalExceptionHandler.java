package store._0982.product.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import store._0982.product.common.dto.ResponseDto;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<String>> handleCustomException(CustomException e) {
        log.error("[{}] {}", e.getErrorCode(), e.getMessage(), e);
        HttpStatus httpStatus = e.getErrorCode().getHttpStatus();
        return ResponseEntity.status(httpStatus)
                .body(new ResponseDto<>(httpStatus, null, e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDto<String> handleSecurityException(SecurityException e) {
        log.error("[{}] {}", HttpStatus.FORBIDDEN, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.FORBIDDEN, null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<String> handleException(Exception e) {
        log.error("[{}] {}", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
    }
}
