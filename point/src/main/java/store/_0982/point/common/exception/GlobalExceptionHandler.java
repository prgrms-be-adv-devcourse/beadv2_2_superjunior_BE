package store._0982.point.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import store._0982.point.common.dto.ResponseDto;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR_LOG_FORMAT = "[{}] {}";

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<String>> handleCustomException(CustomException e) {
        log.error(ERROR_LOG_FORMAT, e.getErrorCode(), e.getMessage(), e);
        HttpStatus httpStatus = e.getErrorCode().getHttpStatus();
        return ResponseEntity.status(httpStatus)
                .body(new ResponseDto<>(httpStatus, null, e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseDto<String> handleSecurityException(SecurityException e) {
        log.error(ERROR_LOG_FORMAT, HttpStatus.FORBIDDEN, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.FORBIDDEN, null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDto<String> handleException(Exception e) {
        log.error(ERROR_LOG_FORMAT, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error(ERROR_LOG_FORMAT, HttpStatus.NOT_FOUND, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.NOT_FOUND, null, e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseDto<String> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error(ERROR_LOG_FORMAT, HttpStatus.NOT_FOUND, e.getMessage(), e);
        return new ResponseDto<>(HttpStatus.NOT_FOUND, null, e.getMessage());
    }
}
