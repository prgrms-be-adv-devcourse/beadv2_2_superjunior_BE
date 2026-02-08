package store._0982.commerce.infrastructure.grouppurchase;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLike;

import java.util.UUID;

public interface GroupPurchaseLikeJpaRepository extends JpaRepository<GroupPurchaseLike, UUID> {
    boolean existsByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);
}
