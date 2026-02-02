package store._0982.commerce.domain.sellerbalance;

import store._0982.common.domain.sellerbalance.SellerBalance;

import java.util.Optional;
import java.util.UUID;

public interface SellerBalanceRepository {

    Optional<SellerBalance> findByMemberId(UUID sellerId);

    void save(SellerBalance sellerBalance);

}
