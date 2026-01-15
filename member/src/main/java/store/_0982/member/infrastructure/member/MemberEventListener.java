package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.MemberDeletedEvent;
import store._0982.member.application.member.event.MemberDeletedServiceEvent;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemberEventListener {

    private final KafkaTemplate<UUID, MemberDeletedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(MemberDeletedServiceEvent event) {
        MemberDeletedEvent kafkaEvent = MemberEventMapper.createEvent(event);

        kafkaTemplate.send(
                KafkaTopics.MEMBER_DELETED,
                kafkaEvent.getMemberId(),
                kafkaEvent
        );
    }
}
