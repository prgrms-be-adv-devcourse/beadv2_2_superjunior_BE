package store._0982.elasticsearch.infrastructure.queryfactory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupPurchaseSearchQueryFactory {

    public NativeQuery createSearchQuery(
            String keyword,
            String status,
            Pageable pageable
    ) {
        boolean noKeyword = (keyword == null || keyword.isBlank());

        // 🔹 1) keyword 없음 → 전체 문서 + status 필터만
        if (noKeyword) {
            return NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> {

                        // 전체 문서
                        b.must(m -> m.matchAll(mm -> mm));

                        // status 선택 필터
                        if (status != null && !status.isBlank()) {
                            b.filter(f -> f.term(t -> t
                                    .field("status")
                                    .value(status)
                            ));
                        }

                        return b;
                    }))
                    .withPageable(pageable)
                    .build();
        }

        // 🔹 2) keyword 있음 → phrase + prefix + fuzzy + match
        return NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 1. 정확 문구 검색 (title 우선)
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

                    // 2. prefix (자동완성 느낌)
                    b.should(s -> s.matchPhrasePrefix(mpp -> mpp
                            .field("title")
                            .query(keyword)
                            .boost(3.0f)
                    ));

                    // 3. fuzzy (오타 허용)
                    b.should(s -> s.match(m -> m
                            .field("title")
                            .query(keyword)
                            .fuzziness("AUTO")
                            .boost(1.5f)
                    ));

                    // 4. 일반 match (description)
                    b.should(s -> s.match(m -> m
                            .field("description")
                            .query(keyword)
                            .boost(1.0f)
                    ));

                    // 🔥 status 필터 (선택)
                    if (status != null && !status.isBlank()) {
                        b.filter(f -> f.term(t -> t
                                .field("status")
                                .value(status)
                        ));
                    }

                    // should 조건 중 하나는 반드시 매칭
                    b.minimumShouldMatch("1");

                    return b;
                }))
                .withPageable(pageable)
                .build();
    }
}
