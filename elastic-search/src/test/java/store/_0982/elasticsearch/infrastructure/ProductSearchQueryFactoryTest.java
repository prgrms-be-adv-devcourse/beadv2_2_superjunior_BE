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
    @DisplayName("keyword가 비어있으면 match_all + sellerId 필터 쿼리 생성")
    void build_matchAllQuery_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NativeQuery query = factory.build(null, sellerId, null, pageable);

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_all")
                .contains("sellerId")
                .contains(sellerId.toString());

        assertThat(query.getPageable().getPageSize()).isEqualTo(10);
        assertThat(query.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("keyword가 있으면 phrase/prefix/fuzzy/match should과 sellerId, category 필터 포함")
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
                .contains("match_phrase")
                .contains("match_phrase_prefix")
                .contains("fuzziness")
                .contains("match")
                .contains("sellerId")
                .contains(sellerId.toString())
                .contains("category")
                .contains(category)
                .contains("minimum_should_match");

        assertThat(query.getPageable().getPageSize()).isEqualTo(5);
        assertThat(query.getPageable().getPageNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("category가 비어있으면 category 필터가 추가되지 않음")
    void build_withoutCategory_success() {
        // given
        UUID sellerId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        String keyword = "abc";

        // when
        NativeQuery query = factory.build(keyword, sellerId, "", pageable);

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .doesNotContain("category")
                .contains("sellerId");
    }
}
