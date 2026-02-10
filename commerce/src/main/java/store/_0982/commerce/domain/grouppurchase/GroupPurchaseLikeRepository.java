package store._0982.commerce.domain.grouppurchase;

import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseLikeRepository {
    GroupPurchaseLike save(GroupPurchaseLike groupPurchaseLike);

    boolean existsByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);

    Optional<GroupPurchaseLike> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);

    void delete(GroupPurchaseLike groupPurchaseLike);
}
