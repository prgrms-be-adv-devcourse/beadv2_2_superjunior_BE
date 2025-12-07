package store._0982.point.common.exception;

import lombok.Getter;

@Getter
public class PaymentClientException extends RuntimeException {
    private final String code;

    public PaymentClientException(String code, String message) {
        super(message);
        this.code = code;
    }
}
