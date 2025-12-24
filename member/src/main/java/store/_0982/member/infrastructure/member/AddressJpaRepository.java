package store._0982.member.infrastructure.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.member.Address;

import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<Address, UUID> {
    Page<Address> findAllByMemberId(Pageable pageable, UUID memberId);
}
