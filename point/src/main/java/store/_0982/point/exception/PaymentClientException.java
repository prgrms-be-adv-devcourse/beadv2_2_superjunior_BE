package store._0982.point.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 토스페이먼츠 API에서 반환하는 에러를 정의한 클래스입니다.
 *
 * @author Minhyung Kim
 */
@Getter
public class PaymentClientException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public PaymentClientException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
