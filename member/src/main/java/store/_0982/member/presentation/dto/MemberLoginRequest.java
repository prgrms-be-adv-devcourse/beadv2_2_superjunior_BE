package store._0982.member.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import store._0982.member.application.dto.MemberLoginCommand;

public record MemberLoginRequest(
        @Email
        String email,
        @NotEmpty
        String password
) {
    public MemberLoginCommand toCommand() {
        return new MemberLoginCommand(email(), password());
    }
}
