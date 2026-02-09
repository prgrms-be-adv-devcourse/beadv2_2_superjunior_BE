package store._0982.batch.domain.recommendation;

import store._0982.common.domain.vector.PersonalVector;

public interface PersonalVectorRepository {
    void saveAll(Iterable<? extends PersonalVector> vectors);
}
