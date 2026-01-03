package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.member.Address;
import store._0982.member.domain.member.AddressRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepository {

    private final AddressJpaRepository addressJpaRepository;

    @Override
    public Address save(Address address) {
        return addressJpaRepository.save(address);
    }

    @Override
    public Page<Address> findAllByMemberId(Pageable pageable, UUID memberId) {
        return addressJpaRepository.findAllByMemberId(pageable, memberId);
    }

    @Override
    public Optional<Address> findById(UUID addressId) {
        return addressJpaRepository.findById(addressId);
    }

    @Override
    public void deleteById(UUID addressId) {
        addressJpaRepository.deleteById(addressId);
    }
}
