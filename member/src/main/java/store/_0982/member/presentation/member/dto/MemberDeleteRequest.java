package store._0982.member.presentation.member.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberDeleteRequest(
        @NotBlank
        String password
) {
}
