package store._0982.batch.domain.ai;

public interface PersonalVectorRepository {
    void saveAll(Iterable<? extends PersonalVector> vectors);
}
