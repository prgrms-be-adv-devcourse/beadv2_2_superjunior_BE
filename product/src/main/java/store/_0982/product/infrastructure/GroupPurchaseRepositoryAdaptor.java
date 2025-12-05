package store._0982.product.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupPurchaseRepositoryAdaptor implements GroupPurchaseRepository {

    private final GroupPurchaseJpaRepository groupPurchaseJpaRepository;

    @Override
    public Optional<GroupPurchase> findById(UUID groupPurchaseId) {
        return groupPurchaseJpaRepository.findById(groupPurchaseId);
    }
}
