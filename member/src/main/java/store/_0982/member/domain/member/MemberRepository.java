package store._0982.member.domain.member;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepository {
    Member save(Member member);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(UUID memberId);

    Optional<Member> findByName(String name);

    void hardDelete(Member member);

    Page<UUID> findIds(Pageable pageable);
}
