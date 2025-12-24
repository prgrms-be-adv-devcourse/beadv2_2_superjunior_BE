package store._0982.member.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.member.Seller;

import java.util.UUID;

public interface SellerJpaRepository extends JpaRepository<Seller, UUID> {
}
