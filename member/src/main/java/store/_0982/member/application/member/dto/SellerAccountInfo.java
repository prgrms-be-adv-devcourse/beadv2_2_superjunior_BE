package store._0982.member.application.member.dto;

import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.Seller;

import java.util.UUID;

public record SellerAccountInfo(
        UUID sellerId,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String businessRegistrationNumber,
        String phoneNumber,
        String email
) {

    public static SellerAccountInfo from(Seller seller) {
        Member member = seller.getMember();
        return new SellerAccountInfo(
                seller.getSellerId(),
                seller.getBankCode(),
                seller.getAccountNumber(),
                seller.getAccountHolder(),
                seller.getBusinessRegistrationNumber(),
                member.getPhoneNumber(),
                member.getEmail()
        );
    }
}
