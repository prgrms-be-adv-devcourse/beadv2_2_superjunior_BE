package store._0982.member.application.dto;

import store._0982.member.domain.Seller;

import java.util.UUID;

public record SellerBalanceInfo(UUID sellerId, int balance) {
    public static SellerBalanceInfo from(Seller seller) {
        return new SellerBalanceInfo(seller.getSellerId(), seller.getSettlementBalance());
    }
}
