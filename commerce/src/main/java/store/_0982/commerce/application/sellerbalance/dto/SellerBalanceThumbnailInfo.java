package store._0982.commerce.application.sellerbalance.dto;

import store._0982.commerce.domain.sellerbalance.SellerBalance;

import java.util.UUID;

public record SellerBalanceThumbnailInfo (
        UUID sellerId
){
    public static SellerBalanceThumbnailInfo from(SellerBalance balance) {
        return new SellerBalanceThumbnailInfo(
                balance.getMemberId());
    }
}
