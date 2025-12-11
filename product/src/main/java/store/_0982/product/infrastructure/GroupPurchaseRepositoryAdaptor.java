package store._0982.product.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public GroupPurchase save(GroupPurchase groupPurchase) {
        return groupPurchaseJpaRepository.save(groupPurchase);
    }

    @Override
    public Optional<GroupPurchase> findById(UUID groupPurchaseId) {
        return groupPurchaseJpaRepository.findById(groupPurchaseId);
    }

    @Override
    public Page<GroupPurchase> findAll(Pageable pageable) {
        return groupPurchaseJpaRepository.findAll(pageable);
    }

    @Override
    public Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable) {
        return groupPurchaseJpaRepository.findAllBySellerId(sellerId, pageable);
    }

    @Override
    public void delete(GroupPurchase groupPurchase) {
        groupPurchaseJpaRepository.delete(groupPurchase);
    }

    @Override
    public GroupPurchase saveAndFlush(GroupPurchase groupPurchase) {
        return groupPurchaseJpaRepository.saveAndFlush(groupPurchase);
    }

}
