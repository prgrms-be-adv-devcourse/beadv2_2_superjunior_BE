package store._0982.member.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.Member;

import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByName(String name);
}
