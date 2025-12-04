package store._0982.point.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final CustomErrorCode errorCode;

    public CustomException(CustomErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
