package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryAdapter implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    public Optional<Member> findById(UUID memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Override
    public Optional<Member> findByName(String name) {
        return memberJpaRepository.findByName(name);
    }

    @Override
    public void hardDelete(Member member) {
        memberJpaRepository.delete(member);
    }

    @Override
    public Page<UUID> findIds(Pageable pageable) {
        return memberJpaRepository.findAllIds(pageable);
    }
}
