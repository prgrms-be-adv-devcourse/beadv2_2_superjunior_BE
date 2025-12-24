package store._0982.batch.domain.sellerbalance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SellerBalanceHistoryRepository {

    void save(SellerBalanceHistory sellerBalanceHistory);

    Page<SellerBalanceHistory> findAllMemberId(UUID memberId, Pageable pageable);

}
