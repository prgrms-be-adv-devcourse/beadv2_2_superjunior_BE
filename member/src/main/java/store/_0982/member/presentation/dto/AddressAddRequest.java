package store._0982.member.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
        @Pattern(regexp = "^[°Ą-ĆRa-zA-Z\\s]+$")
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
