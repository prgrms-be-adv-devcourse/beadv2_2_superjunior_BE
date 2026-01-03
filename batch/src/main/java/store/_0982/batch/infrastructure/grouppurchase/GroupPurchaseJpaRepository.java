package store._0982.batch.infrastructure.grouppurchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupPurchaseJpaRepository extends JpaRepository<GroupPurchase, UUID> {
    List<GroupPurchase> findAllByStatusAndStartDateBefore(GroupPurchaseStatus status, OffsetDateTime now);
}
