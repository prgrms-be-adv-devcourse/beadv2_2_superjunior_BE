package store._0982.member.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository {
    Address save(Address address);

    Page<Address> findAllByMemberId(Pageable pageable, UUID memberId);

    Optional<Address> findById(UUID addressId);

    void deleteById(UUID addressId);
}
