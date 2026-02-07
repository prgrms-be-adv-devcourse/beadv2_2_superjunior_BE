package store._0982.commerce.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanceledOrderRepository {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    void save(CanceledOrder canceledOrder);

    List<CanceledOrder> findAllByStatusInAndCanceledAtBefore(List<CancelStatus> pendingStatuses, OffsetDateTime minutesAgo);

    Optional<CanceledOrder> findByOrderId(UUID orderId);

    Page<CanceledOrder> findAllByMemberId(UUID memberId, Pageable pageable);

    Page<CanceledOrder> findAllBySellerIdAndStatus(UUID sellerId, CancelStatus cancelStatus, Pageable pageable);
}
