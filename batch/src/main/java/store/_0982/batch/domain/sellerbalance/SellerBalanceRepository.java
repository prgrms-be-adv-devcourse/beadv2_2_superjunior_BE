package store._0982.batch.domain.sellerbalance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerBalanceRepository {

    Optional<SellerBalance> findByMemberId(UUID sellerId);

    List<SellerBalance> findAllByMemberIdIn(List<UUID> memberIds);

    void save(SellerBalance sellerBalance);

    void saveAll(List<SellerBalance> sellerBalances);
}
