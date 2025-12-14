package store._0982.elasticsearch.infrastructure;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import store._0982.elasticsearch.infrastructure.queryfactory.ProductSearchQueryFactory;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSearchQueryFactoryTest {

    private final ProductSearchQueryFactory factory = new ProductSearchQueryFactory();

    @Test
    @DisplayName("keyword가 null이면 match_all + sellerId 필터 쿼리가 생성된다")
    void build_matchAllQuery_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NativeQuery query = factory.build(null, sellerId, null, pageable);

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q).contains("match_all")
                .contains("sellerId")
                .contains(sellerId.toString());

        // pageable
        assertThat(query.getPageable().getPageSize()).isEqualTo(10);
        assertThat(query.getPageable().getPageNumber()).isZero();
    }


    @Test
    @DisplayName("keyword가 있으면 phrase, prefix, fuzzy, match 등이 포함된 bool 쿼리가 생성된다")
    void build_keywordQuery_success() {
        // given
        String keyword = "test";
        UUID sellerId = UUID.randomUUID();
        String category = "KIDS";
        Pageable pageable = PageRequest.of(1, 5);

        // when
        NativeQuery query = factory.build(keyword, sellerId, category, pageable);

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();


        assertThat(q)
                // should 절 테스트
                .contains("match_phrase")
                .contains("match_phrase_prefix")
                .contains("fuzziness")
                .contains("AUTO")
                .contains("match")
                // 필터 확인
                .contains("sellerId")
                .contains(sellerId.toString())
                .contains("category")
                .contains(category)
                // minimumShouldMatch 확인
                .contains("minimum_should_match");
    }

    @Test
    @DisplayName("category 없을 때 category 필터는 추가되지 않는다")
    void build_withoutCategory_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "abc";

        // when
        NativeQuery query = factory.build(keyword, sellerId, null, pageable);

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q).doesNotContain("category")
                .contains("sellerId");
    }
}
