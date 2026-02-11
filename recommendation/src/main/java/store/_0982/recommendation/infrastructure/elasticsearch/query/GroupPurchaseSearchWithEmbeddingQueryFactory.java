package store._0982.recommendation.infrastructure.elasticsearch.query;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupPurchaseSearchWithEmbeddingQueryFactory {

    public NativeQuery createKnnQueryWithIds(
            float[] vector,
            List<String> ids,
            Pageable pageable
    ) {
        List<Float> queryVector = toFloatList(vector);
        if (queryVector.isEmpty()) {
            throw new IllegalArgumentException("vector is null or empty");
        }

        int k = pageable.getPageSize();
        int numCandidates = Math.max(k * 5, k);
        List<FieldValue> idValues = ids.stream()
                .map(FieldValue::of)
                .toList();

        return new NativeQueryBuilder()
                .withKnnSearches(KnnSearch.of(knn -> knn
                        .field("productVector")
                        .queryVector(queryVector)
                        .k(k)
                        .numCandidates(numCandidates)
                        .filter(f -> f.terms(t -> t
                                .field("groupPurchaseId")
                                .terms(v -> v.value(idValues))
                        ))
                ))
                .withPageable(pageable)
                .build();
    }

    public NativeQuery createKnnQuery(
            float[] vector,
            String status,
            Pageable pageable
    ) {
        List<Float> queryVector = toFloatList(vector);
        if (queryVector.isEmpty()) {
            throw new IllegalArgumentException("vector is null or empty");
        }

        int k = pageable.getPageSize();
        int numCandidates = Math.max(k * 5, k);

        return new NativeQueryBuilder()
                .withKnnSearches(KnnSearch.of(knn -> knn
                        .field("productVector")
                        .queryVector(queryVector)
                        .k(k)
                        .numCandidates(numCandidates)
                        .filter(f -> {
                            if (status == null || status.isBlank()) {
                                return f.matchAll(m -> m);
                            }
                            return f.term(t -> t.field("status").value(status));
                        })
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
