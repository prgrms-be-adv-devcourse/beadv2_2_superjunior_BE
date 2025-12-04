package store._0982.member.presentation.dto;

import store._0982.member.application.dto.MemberLoginCommand;

public record MemberLoginRequest(
        String email,
        String password
) {
    public MemberLoginCommand toCommand() {
        return new MemberLoginCommand(email(), password());
    }
}
