package store._0982.dummy.object.member.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.member.domain.member.Seller;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "sellerId",
        "createdAt",
        "updatedAt",
        "bankCode",
        "accountNumber",
        "accountHolder",
        "businessRegistrationNumber",
        "status"
})
public record SellerRowCsv(
        UUID sellerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String businessRegistrationNumber,
        Seller.Status status
) {
    public static SellerRowCsv from(Seller seller) {
        return new SellerRowCsv(
                seller.getSellerId(),
                seller.getCreatedAt(),
                seller.getUpdatedAt(),
                seller.getBankCode(),
                seller.getAccountNumber(),
                seller.getAccountHolder(),
                seller.getBusinessRegistrationNumber(),
                seller.getStatus() == null ? null : seller.getStatus()
        );
    }

}
