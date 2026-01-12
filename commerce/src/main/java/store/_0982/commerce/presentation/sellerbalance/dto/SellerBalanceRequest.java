package store._0982.commerce.presentation.sellerbalance.dto;

import jakarta.validation.constraints.NotNull;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceCommand;

import java.util.UUID;

public record SellerBalanceRequest (
        @NotNull
        UUID sellerId
)
{
    public SellerBalanceCommand toCommand() {
        return new SellerBalanceCommand(sellerId);
    }
}
