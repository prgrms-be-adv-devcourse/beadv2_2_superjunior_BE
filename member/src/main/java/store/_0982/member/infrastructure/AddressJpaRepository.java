package store._0982.member.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.Address;

import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<Address, UUID> {
    Page<Address> findAllByMemberId(Pageable pageable, UUID memberId);
}
