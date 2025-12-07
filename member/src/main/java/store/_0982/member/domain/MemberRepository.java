package store._0982.member.domain;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(UUID memberId);

    Optional<Member> findByName(String name);
}
