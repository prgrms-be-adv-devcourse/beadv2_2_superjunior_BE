package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.Point;
import store._0982.point.domain.repository.PointRepository;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class PointRepositoryAdapter implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<Point> findById(UUID memberId) {
        return pointJpaRepository.findById(memberId);
    }

    @Override
    public Point save(Point afterPayment) {
        return pointJpaRepository.save(afterPayment);
    }
}
