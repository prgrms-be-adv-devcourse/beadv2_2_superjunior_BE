package store._0982.member.infrastructure.member;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import store._0982.common.kafka.dto.MemberDeletedEvent;
import store._0982.member.application.member.event.MemberDeletedServiceEvent;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class MemberEventMapper {
    public static MemberDeletedEvent createEvent(MemberDeletedServiceEvent event){
        return new MemberDeletedEvent(event.memberId());
    }
}
