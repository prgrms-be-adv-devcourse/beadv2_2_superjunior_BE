package store._0982.elasticsearch.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GroupPurchaseSearchQueryFactoryTest {

    private final GroupPurchaseSearchQueryFactory factory =
            new GroupPurchaseSearchQueryFactory();

    @Test
    @DisplayName("keyword가 null이면 match_all + status 필터 쿼리가 생성된다")
    void create_matchAllQuery_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String status = "OPEN";
        String category = "HOME";
        String memberId = "";

        // when
        NativeQuery query = factory.createSearchQuery(
                null,
                status,
                category,
                memberId,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_all")
                .contains("status")
                .contains(status);

        // pageable
        assertThat(query.getPageable().getPageSize()).isEqualTo(10);
        assertThat(query.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("keyword가 있으면 phrase, prefix, fuzzy, match 등이 포함된 bool 쿼리가 생성된다")
    void create_keywordQuery_success() {
        // given
        String keyword = "아이폰";
        Pageable pageable = PageRequest.of(1, 5);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                null,
                null,
                null,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                // should 절
                .contains("match_phrase")
                .contains("match_phrase_prefix")
                .contains("fuzziness")
                .contains("AUTO")
                .contains("match")
                // minimumShouldMatch
                .contains("minimum_should_match");

        // pageable
        assertThat(query.getPageable().getPageSize()).isEqualTo(5);
        assertThat(query.getPageable().getPageNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("keyword와 status가 함께 있으면 should 쿼리 + status 필터가 생성된다")
    void create_keywordAndStatus_success() {
        // given
        String keyword = "갤럭시";
        String status = "CLOSED";
        String category = "HOME";
        String memberId = "1";
        Pageable pageable = PageRequest.of(0, 20);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                status,
                category,
                memberId,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("should")
                .contains("status")
                .contains(status)
                .contains("minimum_should_match");
    }

    @Test
    @DisplayName("status가 없으면 status 필터는 추가되지 않는다")
    void create_withoutStatus_success() {
        // given
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                null,
                null,
                null,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .doesNotContain("status")
                .contains("match_phrase");
    }
}
