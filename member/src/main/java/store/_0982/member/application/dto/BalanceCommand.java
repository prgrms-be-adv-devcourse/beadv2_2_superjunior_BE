package store._0982.member.application.dto;

import store._0982.member.domain.Role;

import java.util.UUID;

public record BalanceCommand(UUID memberId, Role role) {
}
