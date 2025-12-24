package store._0982.member.application.member.dto;

import store._0982.member.domain.member.Address;
import store._0982.member.domain.member.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AddressInfo(
        UUID addressId,
        UUID memberId,
        String address,
        String addressDetail,
        String postalCode,
        String receiverName,
        String phoneNumber,
        OffsetDateTime createdAt
) {
    public static AddressInfo from(Address address) {
        Member member = address.getMember();
        return new AddressInfo(
                address.getAddressId(),
                member.getMemberId(),
                address.getAddress(),
                address.getAddressDetail(),
                address.getPostalCode(),
                address.getReceiverName(),
                address.getPhoneNumber(),
                address.getCreatedAt()
        );
    }
}
