package store._0982.member.presentation.dto;

import store._0982.member.application.dto.MemberSignUpCommand;

public record MemberSignUpRequest(
    String email,
    String password,
    String name,
    String phoneNumber
) {
    public MemberSignUpCommand toCommand() {
        return new MemberSignUpCommand(email(), password(), name(), phoneNumber());
    }
}
