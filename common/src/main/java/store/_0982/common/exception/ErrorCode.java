package store._0982.common.exception;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 구현체를 Enum 클래스로만 설정해 주세요.
 *
 * @author Minhyung Kim
 */
public interface ErrorCode extends Serializable {
    HttpStatus getHttpStatus();

    String getMessage();
}
