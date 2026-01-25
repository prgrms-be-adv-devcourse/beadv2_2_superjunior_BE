package store._0982.member.presentation.member.dto;

import store._0982.member.application.member.EmailVerificationCommand;

public record EmailVerificationRequest(String email, String token) {
    public EmailVerificationCommand toCommand() {
        return new EmailVerificationCommand(email, token);
    }
}
