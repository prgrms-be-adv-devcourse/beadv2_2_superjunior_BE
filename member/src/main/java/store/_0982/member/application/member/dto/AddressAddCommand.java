package store._0982.member.application.member.dto;

import java.util.UUID;

public record AddressAddCommand(
        UUID memberId,
        String address,
        String addressDetail,
        String postalCode,
        String receiverName,
        String phoneNumber
) {
}
