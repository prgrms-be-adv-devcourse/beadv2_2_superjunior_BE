package store._0982.elasticsearch.infrastructure.queryfactory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductSearchQueryFactory {

    public NativeQuery build(String keyword, UUID sellerId, String category, Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {
            return buildMatchAllQuery(sellerId, category, pageable);
        } else {
            return buildKeywordSearchQuery(keyword, sellerId, category, pageable);
        }
    }

    private NativeQuery buildMatchAllQuery(UUID sellerId, String category, Pageable pageable) {
        return NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.must(m -> m.matchAll(mp -> mp));
                    b.filter(f -> f.term(t -> t.field("sellerId").value(sellerId.toString())));

                    if (category != null && !category.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("category").value(category)));
                    }
                    return b;
                }))
                .withPageable(pageable)
                .build();
    }

    private NativeQuery buildKeywordSearchQuery(String keyword, UUID sellerId, String category, Pageable pageable) {

        return NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 1. 정확 phrase 매칭
                    b.should(s -> s.matchPhrase(mp -> mp.field("name").query(keyword).boost(5.0f)));
                    b.should(s -> s.matchPhrase(mp -> mp.field("description").query(keyword).boost(2.0f)));

                    // 2. prefix 검색
                    b.should(s -> s.matchPhrasePrefix(mpp -> mpp.field("name").query(keyword).boost(3.0f)));

                    // 3. fuzzy 오타 검색
                    b.should(s -> s.match(m -> m.field("name").query(keyword).fuzziness("AUTO").boost(1.5f)));

                    // 4. 일반 match 검색
                    b.should(s -> s.match(m -> m.field("description").query(keyword).boost(1.0f)));

                    // 필터들
                    b.filter(f -> f.term(t -> t.field("sellerId").value(sellerId.toString())));
                    if (category != null && !category.isBlank()) {
                        b.filter(f -> f.term(t -> t.field("category").value(category)));
                    }

                    b.minimumShouldMatch("1");
                    return b;
                }))
                .withPageable(pageable)
                .build();
    }
}
