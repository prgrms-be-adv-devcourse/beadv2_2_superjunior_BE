package store._0982.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.LogFormat;

/**
 * 기본적인 에러 핸들링을 정의한 핸들러입니다.
 * <p>각 모듈에서 이 클래스를 상속시킨 뒤, {@link org.springframework.web.bind.annotation.RestControllerAdvice} 어노테이션을 이용해 주세요.</p>
 * <p>
 * 코드 예시
 * <pre>
 * {@code
 * @RestControllerAdvice
 * public class CustomExceptionHandler extends BaseExceptionHandler {
 *     // 추가하거나 변경할 메서드 작성
 * }}
 * </pre>
 *
 * @author Minhyung Kim
 */
@Slf4j
public abstract class BaseExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<String>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus httpStatus = errorCode.getHttpStatus();
        log.error(LogFormat.errorOf(httpStatus, e.getMessage()), e);
        return ResponseEntity.status(httpStatus)
                .body(new ResponseDto<>(httpStatus, null, e.getMessage()));
    }

    /**
     * &#064;Valid  어노테이션에 의한 검증 에러 핸들러입니다.
     * <p>필요한 경우 오버라이드해 사용하세요.</p>
     */
    @SuppressWarnings("unused")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<String>> handleInvalidArgumentException(MethodArgumentNotValidException e) {
        return handleCustomException(new CustomException(DefaultErrorCode.INVALID_PARAMETER));
    }

    // @RequestHeader 어노테이션에 의한 검증 에러 핸들러
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseDto<String>> handleMissingHeaderException(MissingRequestHeaderException e) {
        CustomException ex = switch (e.getHeaderName()) {
            case HeaderName.ID -> new CustomException(DefaultErrorCode.NO_LOGIN_INFO);
            case HeaderName.EMAIL -> new CustomException(DefaultErrorCode.NO_EMAIL_INFO);
            case HeaderName.ROLE -> new CustomException(DefaultErrorCode.NO_ROLE_INFO);
            case HeaderName.TOKEN -> new CustomException(DefaultErrorCode.NO_TOKEN_INFO);
            default -> new CustomException(DefaultErrorCode.REQUEST_HEADER_IS_NULL);
        };
        return handleCustomException(ex);
    }

    // TODO: 커스텀 에러로 변환해서 메시지를 유저 친화적이게 구성할까?
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
