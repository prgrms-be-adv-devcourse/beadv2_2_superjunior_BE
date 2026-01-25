package store._0982.member.application.member;


import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

public interface CommerceQueryPort {
    ResponseDto<Void> postSellerBalance(UUID id);

    List<UUID> getGroupPurchaseParticipants(UUID groupPurchaseId);
}
