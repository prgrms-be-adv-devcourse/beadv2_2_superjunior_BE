package store._0982.member.application.member.dto;

import java.util.UUID;

public record SellerCommand(UUID userMemberId, UUID searchedSellerId) {
}
