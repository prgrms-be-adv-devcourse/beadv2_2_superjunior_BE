package store._0982.batch.infrastructure.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;
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
  
    @Override
    public List<GroupPurchase> findByStatusAndSettledAtIsNull(GroupPurchaseStatus status) {
        return groupPurchaseJpaRepository.findByStatusAndSettledAtIsNull(status);
    }

    @Override
    public List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList) {
        return groupPurchaseJpaRepository.saveAll(groupPurchaseList);
    }

    @Override
    @Transactional
    public int openReadyGroupPurchases(OffsetDateTime now) {
        return groupPurchaseJpaRepository.openReadyGroupPurchases(now);
    }

    @Override
    public boolean existsByProductId(UUID productId) {
        return groupPurchaseJpaRepository.existsByProductId((productId));
    }

    @Override
    public boolean existsByProductIdAndStatusIn(UUID productId, List<GroupPurchaseStatus> statuses) {
        return groupPurchaseJpaRepository.existsByProductIdAndStatusIn(productId, statuses);
    }

    @Override
    public List<GroupPurchase> findAllByStatusAndStartDateBefore(GroupPurchaseStatus status, OffsetDateTime now) {
        return groupPurchaseJpaRepository.findAllByStatusAndStartDateBefore(status, now);
    }

    @Override
    public List<GroupPurchase> findAllByGroupPurchaseIdIn(List<UUID> groupPurchaseIds) {
        return groupPurchaseJpaRepository.findAllByGroupPurchaseIdIn(groupPurchaseIds);
    }
}
