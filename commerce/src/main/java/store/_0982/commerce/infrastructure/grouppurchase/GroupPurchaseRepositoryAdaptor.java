package store._0982.commerce.infrastructure.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.Product;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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

    @Override
    public List<GroupPurchaseSearchRow> findSearchRowsByIds(List<UUID> groupPurchaseIds) {
        List<Object[]> rows = groupPurchaseJpaRepository.findSearchRowsByIdsWithProduct(groupPurchaseIds);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<GroupPurchaseSearchRow> result = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            GroupPurchase groupPurchase = (GroupPurchase) row[0];
            Product product = (Product) row[1];
            result.add(new GroupPurchaseSearchRow(
                    groupPurchase.getGroupPurchaseId(),
                    groupPurchase.getMinQuantity(),
                    groupPurchase.getMaxQuantity(),
                    groupPurchase.getTitle(),
                    groupPurchase.getDescription(),
                    groupPurchase.getImageUrl(),
                    groupPurchase.getDiscountedPrice(),
                    groupPurchase.getStatus().name(),
                    groupPurchase.getStartDate().toInstant(),
                    groupPurchase.getEndDate().toInstant(),
                    groupPurchase.getCreatedAt().toInstant(),
                    groupPurchase.getUpdatedAt() == null ? null : groupPurchase.getUpdatedAt().toInstant(),
                    groupPurchase.getCurrentQuantity(),
                    product.getProductId(),
                    product.getCategory().name(),
                    product.getPrice(),
                    product.getOriginalUrl(),
                    groupPurchase.getSellerId()
            ));
        }
        return result;
    }

    @Override
    public int increaseQuantity(UUID groupPurchaseId, int quantity) {
        return groupPurchaseJpaRepository.increaseQuantity(groupPurchaseId, quantity);
    }

    @Override
    public int decreaseQuantity(UUID groupPurchaseId, int quantity) {
        return groupPurchaseJpaRepository.decreaseQuantity(groupPurchaseId, quantity);
    }

    @Override
    public void increaseLikeCount(UUID id) {
        groupPurchaseJpaRepository.increaseLikeCount(id);
    }

    @Override
    public void decreaseLikeCount(UUID id) {
        groupPurchaseJpaRepository.decreaseLikeCount(id);
    }
}
