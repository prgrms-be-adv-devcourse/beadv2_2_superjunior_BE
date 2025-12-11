package store._0982.order.domain.settlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerBalanceRepository {

    Optional<SellerBalance> findByMemberId(UUID sellerId);

    void save(SellerBalance sellerBalance);

    List<SellerBalance> findAll();

}
