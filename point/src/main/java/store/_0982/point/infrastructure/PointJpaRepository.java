package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.Point;

import java.util.UUID;

public interface PointJpaRepository extends JpaRepository<Point, UUID> {
}
