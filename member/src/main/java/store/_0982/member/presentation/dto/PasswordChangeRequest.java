package store._0982.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeRequest(
        @NotBlank
        String password,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./]).{8,}$",
                message = "비밀번호는 8자리 이상이며 영어, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        )
        String newPassword
) {
}
