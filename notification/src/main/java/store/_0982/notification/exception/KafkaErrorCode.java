package store._0982.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaErrorCode {
    KAFKA_INVALID_EVENT("잘못된 이벤트가 들어왔습니다.");

    private final String message;
}
