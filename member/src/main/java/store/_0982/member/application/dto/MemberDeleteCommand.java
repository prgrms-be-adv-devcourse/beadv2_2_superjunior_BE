package store._0982.member.application.dto;

import java.util.UUID;

public record MemberDeleteCommand(UUID memberId, String password) {
}
