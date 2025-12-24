package store._0982.commerce.infrastructure.settlement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistory;

import java.util.UUID;

public interface SellerBalanceHistoryJpaRepository extends JpaRepository<SellerBalanceHistory, UUID> {

    Page<SellerBalanceHistory> findAllByMemberId(UUID memberId, Pageable pageable);

}
