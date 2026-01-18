package store._0982.member.application.member.event;

import java.util.UUID;

public record MemberDeletedServiceEvent(
    UUID memberId
) {}
