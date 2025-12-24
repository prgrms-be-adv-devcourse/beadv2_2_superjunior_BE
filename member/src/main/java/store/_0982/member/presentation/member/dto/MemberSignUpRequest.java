package store._0982.member.presentation.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import store._0982.member.application.member.dto.MemberSignUpCommand;

public record MemberSignUpRequest(
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>?,./]).{8,}$",
                message = "비밀번호는 8자리 이상이며 영어, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
        )
        String password,

        @Length(min = 2, max = 50)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$")
        String name,

        @NotBlank
        @Pattern(regexp = "^[0-9\\-]{9,15}$")
        String phoneNumber
) {
    public MemberSignUpCommand toCommand() {
        return new MemberSignUpCommand(email(), password(), name(), phoneNumber());
    }
}
