package store._0982.point.domain.repository;

import store._0982.point.domain.entity.Point;

import java.util.Optional;
import java.util.UUID;

public interface PointRepository {
    Optional<Point> findById(UUID memberId);

    Point save(Point afterPayment);
}
