package store._0982.member.application.dto;

import java.util.UUID;

public record PasswordChangeCommand(
        UUID memberId,
        String password,
        String newPassword
){
}
