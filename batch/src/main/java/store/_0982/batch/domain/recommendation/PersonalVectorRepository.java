package store._0982.batch.domain.recommendation;

public interface PersonalVectorRepository {
    void saveAll(Iterable<? extends PersonalVector> vectors);
}
