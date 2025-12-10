package store._0982.common.kafka.dto;

import lombok.Getter;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Kafka에서 이벤트를 발송할 때 사용되는 기본 이벤트 DTO입니다.
 * <p>이벤트를 발송할 때 해당 클래스를 상속시킨 클래스를 전달해 주세요.</p>
 * <pre>
 * {@code
 * @Getter
 * @AllArgsConstructor
 * public CustomEvent extends BaseEvent {
 *     // 추가 필드, 메서드 작성
 * }
 * }
 * </pre>
 *
 * @author Minhyung Kim
 */
@Getter
@SuppressWarnings("unused")
public abstract class BaseEvent {
    private final UUID eventId = UUID.randomUUID();
    private final OffsetDateTime occurredAt;

    /**
     * 기본 이벤트 DTO 생성자입니다.
     */
    protected BaseEvent() {
        this.occurredAt = OffsetDateTime.now();
    }

    /**
     * 테스트용 이벤트 DTO 생성자입니다.
     *
     * @param clock 테스트용으로 생성한 시간
     */
    protected BaseEvent(Clock clock) {
        this.occurredAt = OffsetDateTime.now(clock);
    }

    public String getEventType() {
        return this.getClass().getSimpleName();
    }
}
