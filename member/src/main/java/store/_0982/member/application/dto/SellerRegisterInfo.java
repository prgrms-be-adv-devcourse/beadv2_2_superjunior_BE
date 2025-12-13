package store._0982.member.application.dto;

import store._0982.member.domain.Seller;

import java.util.UUID;

public record SellerRegisterInfo (
        UUID sellerId,
        String accountNumber,
        String bankCode,
        String accountHolder,
        String businessRegistrationNumber
){
    public static SellerRegisterInfo from(Seller seller){
        return new SellerRegisterInfo(seller.getSellerId(), seller.getAccountNumber(), seller.getBankCode(), seller.getAccountHolder(), seller.getBusinessRegistrationNumber());
    }
}
