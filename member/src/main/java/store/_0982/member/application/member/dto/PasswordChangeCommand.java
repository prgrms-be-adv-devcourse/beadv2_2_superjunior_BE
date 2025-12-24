package store._0982.member.application.member.dto;

import java.util.UUID;

public record PasswordChangeCommand(
        UUID memberId,
        String password,
        String newPassword
){
}
