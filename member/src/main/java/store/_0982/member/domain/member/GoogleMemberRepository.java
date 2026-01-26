package store._0982.member.domain.member;

import java.util.Optional;

public interface GoogleMemberRepository {
    Optional<GoogleMember> findByEmail(String email);
    GoogleMember save(GoogleMember googleMember);
}
