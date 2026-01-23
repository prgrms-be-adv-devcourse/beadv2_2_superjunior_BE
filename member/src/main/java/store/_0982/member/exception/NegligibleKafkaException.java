package store._0982.member.exception;

import lombok.Getter;
import org.springframework.kafka.annotation.RetryableTopic;

/**
 * {@link RetryableTopic}에서 재시도가 필요하지 않은 예외를 발생시키고 싶을 때 사용합니다.
 */
@Getter
public class NegligibleKafkaException extends RuntimeException {

    private final NegligibleKafkaErrorType errorType;

    public NegligibleKafkaException(NegligibleKafkaErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
