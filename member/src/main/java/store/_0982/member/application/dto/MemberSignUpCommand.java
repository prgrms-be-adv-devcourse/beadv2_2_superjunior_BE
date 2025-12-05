package store._0982.member.application.dto;

public record MemberSignUpCommand(
    String email,
    String password,
    String name,
    String phoneNumber) {
}

