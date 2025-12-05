package store._0982.member.presentation.dto;

import jakarta.validation.constraints.NotEmpty;

public record MemberDeleteRequest(
        @NotEmpty
        String password
) {
}
