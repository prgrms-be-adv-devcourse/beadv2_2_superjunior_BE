package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.member.GoogleMember;
import store._0982.member.domain.member.GoogleMemberRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoogleMemberRepositoryAdapter implements GoogleMemberRepository {
    private final GoogleMemberJpaRepository googleMemberJpaRepository;

    @Override
    public Optional<GoogleMember> findByEmail(String email) {
        return googleMemberJpaRepository.findById(email);
    }

    @Override
    public GoogleMember save(GoogleMember googleMember) {
        return googleMemberJpaRepository.save(googleMember);
    }
}
