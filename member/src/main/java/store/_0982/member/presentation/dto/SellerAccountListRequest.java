package store._0982.member.presentation.dto;

import store._0982.member.application.dto.SellerAccountListCommand;

import java.util.List;
import java.util.UUID;

public record SellerAccountListRequest(List<UUID> sellerIds) {
    public SellerAccountListCommand toCommand() {
        return new SellerAccountListCommand(sellerIds);
    }
}
