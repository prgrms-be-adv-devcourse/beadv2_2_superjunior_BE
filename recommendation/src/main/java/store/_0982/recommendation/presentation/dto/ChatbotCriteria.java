package store._0982.recommendation.presentation.dto;

import store._0982.common.domain.product.ProductCategory;

import java.util.List;

public record ChatbotCriteria(
        ProductCategory category,
        Long minPrice,
        Long maxPrice,
        List<String> preferences
) {
}
