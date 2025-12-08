package store._0982.member.application.dto;

import store._0982.member.domain.Member;
import store._0982.member.domain.Seller;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SellerInfo(UUID sellerId, String email, String name, OffsetDateTime createdAt, String imageUrl,
                         String phoneNumber, String accountNumber, String bankCode, String accountHolder,
                         String businessRegistrationNumber) {

    public static SellerInfo from(Seller seller) {
        Member member = seller.getMember();
        return new SellerInfo(seller.getSellerId(), member.getEmail(), member.getName(), seller.getCreatedAt(), member.getImageUrl(), member.getPhoneNumber(), seller.getAccountNumber(), seller.getBankCode(), seller.getAccountHolder(), seller.getBusinessRegistrationNumber());
    }

    public SellerInfo blind() {
        return new SellerInfo(this.sellerId(), this.email(), this.name(), this.createdAt(), this.imageUrl(), null,  // phoneNumber
                null,  // accountNumber
                null,  // bankCode
                null,  // accountHolder
                this.businessRegistrationNumber()   // settlementBalance
        );

    }
}