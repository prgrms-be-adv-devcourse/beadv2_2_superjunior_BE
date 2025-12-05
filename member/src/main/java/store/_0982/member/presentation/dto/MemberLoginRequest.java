package store._0982.member.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import store._0982.member.application.dto.MemberLoginCommand;

public record MemberLoginRequest(
        @Email
        String email,
        @NotBlank
        String password
) {
    public MemberLoginCommand toCommand() {
        return new MemberLoginCommand(email(), password());
    }
}
