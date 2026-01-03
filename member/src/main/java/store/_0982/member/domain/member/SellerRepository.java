package store._0982.member.domain.member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(UUID sellerId);

    List<Seller> findAllById(List<UUID> sellerIds);
}
