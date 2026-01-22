package store._0982.member.exception;

import lombok.Getter;

@Getter
public class NegligibleKafkaException extends RuntimeException {

    private final NegligibleKafkaErrorType errorType;

    public NegligibleKafkaException(NegligibleKafkaErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
