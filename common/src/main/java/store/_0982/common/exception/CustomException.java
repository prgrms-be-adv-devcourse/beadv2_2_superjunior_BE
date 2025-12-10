package store._0982.common.exception;

import lombok.Getter;

@Getter
@SuppressWarnings("java:S1948")
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
