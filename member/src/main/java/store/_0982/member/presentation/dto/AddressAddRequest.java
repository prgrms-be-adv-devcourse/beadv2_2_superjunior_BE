package store._0982.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;
import store._0982.member.application.dto.AddressAddCommand;

import java.util.UUID;

public record AddressAddRequest(
        @NotBlank
        @Size(max = 100)
        String address,

        @NotBlank
        @Size(max = 100)
        String addressDetail,

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[0-9]{5}$")
        String postalCode,

        @NotBlank
        @Size(max = 100)
        @Length(min = 2, max = 50)
        @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]+$")
        String receiverName,

        @NotBlank
        @Pattern(regexp = "^[0-9\\-]{9,15}$")
        String phoneNumber

) {

    public AddressAddCommand toCommand(UUID memberId) {
        return new AddressAddCommand(
                memberId,
                address(),
                addressDetail(),
                postalCode(),
                receiverName(),
                phoneNumber()
        );
    }
}
