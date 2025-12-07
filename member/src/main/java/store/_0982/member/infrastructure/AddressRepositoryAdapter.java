package store._0982.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.Address;
import store._0982.member.domain.AddressRepository;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepository {

    private AddressRepository addressRepository;

    @Override
    public Address save(Address address) {
        return addressRepository.save(address);
    }
}
