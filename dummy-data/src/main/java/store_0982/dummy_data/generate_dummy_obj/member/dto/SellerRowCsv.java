package store_0982.dummy_data.generate_dummy_obj.member.dto;

import store._0982.member.domain.member.Seller;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SellerRowCsv(
        String accountHolder,
        String accountNumber,
        String bankCode,
        String businessRegistrationNumber,
        OffsetDateTime createdAt,
        UUID sellerId,
        Seller.Status status,
        OffsetDateTime updatedAt
) {
    public static SellerRowCsv from(Seller seller) {
        return new SellerRowCsv(
                seller.getAccountHolder(),
                seller.getAccountNumber(),
                seller.getBankCode(),
                seller.getBusinessRegistrationNumber(),
                seller.getCreatedAt(),
                seller.getSellerId(),
                seller.getStatus() == null ? null : seller.getStatus(),
                seller.getUpdatedAt()
        );
    }

}
