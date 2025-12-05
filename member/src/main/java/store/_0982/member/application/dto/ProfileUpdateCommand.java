package store._0982.member.application.dto;

import java.util.UUID;

public record ProfileUpdateCommand(UUID memberId, String name, String phoneNumber) {
}
