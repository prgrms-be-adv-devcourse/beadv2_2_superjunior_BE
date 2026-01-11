package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class MemberDeletedEvent extends BaseEvent {

    private UUID memberId;

    public MemberDeletedEvent(Clock clock, UUID memberId) {
        super(clock);
        this.memberId = memberId;
    }
}
