package store._0982.elasticsearch.infrastructure.queryfactory;

import co.elastic.clients.elasticsearch._types.KnnSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GroupPurchaseSearchWithEmbeddingQueryFactory {

    public NativeQuery createSearchQuery(
            String keyword,
            String status,
            String memberId,
            String category,
            float[] vector,
            Pageable pageable
    ) {
        boolean noKeyword = (keyword == null || keyword.isBlank());
        List<Float> queryVector = toFloatList(vector);

        NativeQueryBuilder builder = new NativeQueryBuilder()
                .withQuery(q -> q.bool(b -> {
                    if (noKeyword) {
                        b.must(m -> m.matchAll(mm -> mm));
                    } else {
                        b.should(s -> s.matchPhrase(mp -> mp
                                .field("title")
                                .query(keyword)
                                .boost(5.0f)
                        ));
                        b.should(s -> s.matchPhrase(mp -> mp
                                .field("description")
                                .query(keyword)
                                .boost(2.0f)
                        ));
                        b.should(s -> s.matchPhrasePrefix(mpp -> mpp
                                .field("title")
                                .query(keyword)
                                .boost(3.0f)
                        ));
                        b.should(s -> s.match(m -> m
                                .field("title")
                                .query(keyword)
                                .fuzziness("AUTO")
                                .boost(1.5f)
                        ));
                        b.should(s -> s.match(m -> m
                                .field("description")
                                .query(keyword)
                                .boost(1.0f)
                        ));
                        b.minimumShouldMatch("1");
                    }

                    boolean useQueryFilter = queryVector.isEmpty();
                    if (useQueryFilter) {
                        if (status != null && !status.isBlank()) {
                            b.filter(f -> f.term(t -> t
                                    .field("status")
                                    .value(status)
                            ));
                        }
                        if (category != null && !category.isBlank()) {
                            b.filter(f -> f.nested(n -> n
                                    .path("productDocumentEmbedded")
                                    .query(p -> p.term(t -> t
                                            .field("productDocumentEmbedded.category")
                                            .value(category)
                                    ))
                            ));
                        }
                        if (memberId != null && !memberId.isBlank()) {
                            b.filter(f -> f.nested(n -> n
                                    .path("productDocumentEmbedded")
                                    .query(p -> p.term(t -> t
                                            .field("productDocumentEmbedded.sellerId")
                                            .value(memberId)
                                    ))
                            ));
                        }
                    }

                    return b;
                }))
                .withPageable(pageable);

        if (!queryVector.isEmpty()) {
            int k = pageable.getPageSize();
            int numCandidates = Math.max(k * 5, k);
            builder.withKnnSearches(KnnSearch.of(knn -> {
                knn.field("productVector")
                        .queryVector(queryVector)
                        .k(k)
                        .numCandidates(numCandidates);

                if ((status != null && !status.isBlank())
                        || (category != null && !category.isBlank())
                        || (memberId != null && !memberId.isBlank())) {
                    knn.filter(f -> f.bool(b -> {
                        if (status != null && !status.isBlank()) {
                            b.filter(ff -> ff.term(t -> t
                                    .field("status")
                                    .value(status)
                            ));
                        }
                        if (category != null && !category.isBlank()) {
                            b.filter(ff -> ff.nested(n -> n
                                    .path("productDocumentEmbedded")
                                    .query(p -> p.term(t -> t
                                            .field("productDocumentEmbedded.category")
                                            .value(category)
                                    ))
                            ));
                        }
                        if (memberId != null && !memberId.isBlank()) {
                            b.filter(ff -> ff.nested(n -> n
                                    .path("productDocumentEmbedded")
                                    .query(p -> p.term(t -> t
                                            .field("productDocumentEmbedded.sellerId")
                                            .value(memberId)
                                    ))
                            ));
                        }
                        return b;
                    }));
                }

                return knn;
            }));
        }

        return builder.build();
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
