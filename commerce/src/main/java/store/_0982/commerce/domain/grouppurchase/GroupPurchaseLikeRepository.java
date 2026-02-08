package store._0982.commerce.domain.grouppurchase;

import java.util.UUID;

public interface GroupPurchaseLikeRepository {
    GroupPurchaseLike save(GroupPurchaseLike groupPurchaseLike);

    boolean existsByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);
}
