package store._0982.batch.infrastructure.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;

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
    public List<GroupPurchase> saveAll(List<GroupPurchase> groupPurchaseList) {
        return groupPurchaseJpaRepository.saveAll(groupPurchaseList);
    }
}
