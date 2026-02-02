package store._0982.commerce.domain.sellerbalance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;

import java.util.UUID;

public interface SellerBalanceHistoryRepository {

    void save(SellerBalanceHistory sellerBalanceHistory);

    Page<SellerBalanceHistory> findAllMemberId(UUID memberId, Pageable pageable);

}
