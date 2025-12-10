package store._0982.order.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 주문 API에서 반환하는 에러를 정의한 클래스입니다.
 *
 * @author
 */
@Getter
public class OrderClientException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public OrderClientException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
