package store._0982.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.Address;

import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<Address, UUID> {
}
