package store._0982.commerce.infrastructure.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLike;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLikeRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupPurchaseLikeRepositoryAdaptor implements GroupPurchaseLikeRepository {
    private final GroupPurchaseLikeJpaRepository jpaRepository;


    @Override
    public GroupPurchaseLike save(GroupPurchaseLike groupPurchaseLike) {
        return jpaRepository.save(groupPurchaseLike);
    }

    @Override
    public boolean existsByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId) {
        return jpaRepository.existsByMemberIdAndGroupPurchaseId(memberId, groupPurchaseId);
    }

    @Override
    public Optional<GroupPurchaseLike> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId) {
        return jpaRepository.findByMemberIdAndGroupPurchaseId(memberId, groupPurchaseId);
    }

    @Override
    public void delete(GroupPurchaseLike groupPurchaseLike) {
        jpaRepository.delete(groupPurchaseLike);
    }

}
