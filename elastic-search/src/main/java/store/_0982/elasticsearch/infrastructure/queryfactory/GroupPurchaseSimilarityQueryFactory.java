package store._0982.elasticsearch.infrastructure.queryfactory;

import co.elastic.clients.elasticsearch._types.KnnSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Component
public class GroupPurchaseSimilarityQueryFactory {

    public NativeQuery createSimilarityQuery(
            float[] vector,
            Pageable pageable
    ) {
        List<Float> queryVector = toFloatList(vector);
        if (queryVector.isEmpty()) {
            return new NativeQueryBuilder()
                    .withQuery(q -> q.matchNone(m -> m))
                    .withPageable(pageable)
                    .build();
        }
        int k = pageable.getPageSize();
        int numCandidates = Math.max(k * 5, k);
        return new NativeQueryBuilder()
                .withKnnSearches(KnnSearch.of(knn -> knn
                        .field("productVector")
                        .queryVector(queryVector)
                        .k(k)
                        .numCandidates(numCandidates)
                ))
                .withPageable(pageable)
                .build();
    }

    private List<Float> toFloatList(float[] vector) {
        if (vector == null || vector.length == 0) {
            return List.of();
        }
        List<Float> values = new ArrayList<>(vector.length);
        for (float value : vector) {
            values.add(value);
        }
        return values;
    }
}
