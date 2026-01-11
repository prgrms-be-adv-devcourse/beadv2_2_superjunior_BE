package store._0982.member.exception;

import lombok.Getter;

@Getter
public class CustomKafkaException extends RuntimeException {
    private final KafkaErrorCode errorCode;

    public CustomKafkaException(KafkaErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
