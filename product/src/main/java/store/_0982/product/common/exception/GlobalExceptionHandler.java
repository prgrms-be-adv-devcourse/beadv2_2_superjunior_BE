package store._0982.product.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import store._0982.product.common.dto.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException e) {
        e.printStackTrace();
        return new ResponseEntity<>(e.getErrorCode().getHttpStatus(), null, e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> SecurityExceptionHandler(SecurityException e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.FORBIDDEN, null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, null, e.getMessage());
    }
}