package store._0982.elasticsearch.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import store._0982.elasticsearch.infrastructure.queryfactory.GroupPurchaseSearchQueryFactory;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GroupPurchaseSearchQueryFactoryTest {

    private final GroupPurchaseSearchQueryFactory factory = new GroupPurchaseSearchQueryFactory();

    @Test
    @DisplayName("keyword가 비어있으면 match_all + status/category/seller 필터 조합을 만든다")
    void create_matchAllQuery_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        String status = "OPEN";
        String category = "HOME";
        String memberId = UUID.randomUUID().toString();

        // when
        NativeQuery query = factory.createSearchQuery(
                null,
                status,
                memberId,
                category,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_all")
                .contains("status")
                .contains(status)
                .contains("productDocumentEmbedded.category")
                .contains(category)
                .contains("productDocumentEmbedded.sellerId")
                .contains(memberId);

        assertThat(query.getPageable().getPageSize()).isEqualTo(10);
        assertThat(query.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("keyword가 있으면 phrase/prefix/fuzzy/match should와 status 필터를 포함한다")
    void create_keywordQuery_success() {
        // given
        String keyword = "아이폰";
        String status = "CLOSED";
        Pageable pageable = PageRequest.of(1, 5);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                status,
                null,
                null,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_phrase")
                .contains("match_phrase_prefix")
                .contains("fuzziness")
                .contains("match")
                .contains("status")
                .contains(status)
                .contains("minimum_should_match");

        assertThat(query.getPageable().getPageSize()).isEqualTo(5);
        assertThat(query.getPageable().getPageNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("status가 비어있으면 status 필터를 추가하지 않는다")
    void create_withoutStatus_success() {
        // given
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 15);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                "",
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

    @Test
    @DisplayName("category가 비어있으면 nested category 필터를 추가하지 않는다")
    void create_withoutCategory_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        NativeQuery query = factory.createSearchQuery(
                null,
                "OPEN",
                null,
                "",
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .doesNotContain("productDocumentEmbedded.category")
                .contains("match_all");
    }

    @Test
    @DisplayName("keyword가 있고 category와 sellerId가 있으면 nested 필터가 모두 포함된다")
    void create_keywordWithCategoryAndSeller_success() {
        // given
        String keyword = "헤드폰";
        String status = "OPEN";
        String memberId = UUID.randomUUID().toString();
        String category = "ELECTRONICS";
        Pageable pageable = PageRequest.of(0, 8);

        // when
        NativeQuery query = factory.createSearchQuery(
                keyword,
                status,
                memberId,
                category,
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_phrase")
                .contains("productDocumentEmbedded.category")
                .contains(category)
                .contains("productDocumentEmbedded.sellerId")
                .contains(memberId)
                .contains("status")
                .contains(status)
                .contains("minimum_should_match");

        assertThat(query.getPageable().getPageSize()).isEqualTo(8);
        assertThat(query.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("모든 필터와 keyword가 비어있으면 순수 match_all만 생성된다")
    void create_pureMatchAll_success() {
        // given
        Pageable pageable = PageRequest.of(3, 4);

        // when
        NativeQuery query = factory.createSearchQuery(
                "",
                "",
                "",
                "",
                pageable
        );

        // then
        String q = Objects.requireNonNull(query.getQuery()).toString();

        assertThat(q)
                .contains("match_all")
                .doesNotContain("status")
                .doesNotContain("productDocumentEmbedded.category")
                .doesNotContain("productDocumentEmbedded.sellerId");

        assertThat(query.getPageable().getPageSize()).isEqualTo(4);
        assertThat(query.getPageable().getPageNumber()).isEqualTo(3);
    }
}
