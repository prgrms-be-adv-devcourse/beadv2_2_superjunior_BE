package store._0982.member.domain;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(UUID uuid);
}
