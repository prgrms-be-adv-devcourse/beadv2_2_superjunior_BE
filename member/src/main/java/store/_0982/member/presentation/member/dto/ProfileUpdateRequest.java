package store._0982.member.presentation.member.dto;

import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record ProfileUpdateRequest(
        @Length(min = 2, max = 50)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$")
        String name,
        @Pattern(regexp = "^[0-9\\-]{9,15}$")
        String phoneNumber
) {
}
