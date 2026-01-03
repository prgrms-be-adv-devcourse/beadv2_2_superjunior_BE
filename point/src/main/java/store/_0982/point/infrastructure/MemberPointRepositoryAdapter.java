package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.repository.MemberPointRepository;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class MemberPointRepositoryAdapter implements MemberPointRepository {

    private final MemberPointJpaRepository memberPointJpaRepository;

    @Override
    public Optional<MemberPoint> findById(UUID memberId) {
        return memberPointJpaRepository.findById(memberId);
    }

    @Override
    public MemberPoint save(MemberPoint afterPayment) {
        return memberPointJpaRepository.save(afterPayment);
    }
}
