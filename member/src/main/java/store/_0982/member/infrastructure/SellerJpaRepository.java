package store._0982.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.Seller;

import java.util.UUID;

public interface SellerJpaRepository extends JpaRepository<Seller, UUID> {
}
