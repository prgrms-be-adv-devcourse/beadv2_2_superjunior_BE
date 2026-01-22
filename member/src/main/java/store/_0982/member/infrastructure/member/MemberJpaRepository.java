package store._0982.member.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.member.Member;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByName(String name);

    @Query("select m.memberId from Member m")
    Page<UUID> findAllIds(Pageable pageable);
}
