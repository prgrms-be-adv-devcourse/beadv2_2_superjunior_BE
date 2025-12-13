package store._0982.product.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import store._0982.product.application.dto.GroupPurchaseRegisterCommand;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseRegisterRequest(
        @JsonProperty("minQuantity")
        @NotNull(message = "최소 수량은 필수입니다")
        @Min(value = 1, message = "최소 수량은 1 이상이어야 합니다")
        Integer minQuantity,  // int → Integer

        @JsonProperty("maxQuantity")
        @NotNull(message = "최대 수량은 필수입니다")
        @Min(value = 1, message = "최대 수량은 1 이상이어야 합니다")
        Integer maxQuantity,

        @JsonProperty("title")
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("discountedPrice")
        @NotNull(message = "할인 가격은 필수입니다")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다")
        Integer discountedPrice,

        @JsonProperty("startDate")
        @NotNull(message = "시작일은 필수입니다")
        @FutureOrPresent(message = "시작일은 현재 또는 미래여야 합니다")
        OffsetDateTime startDate,

        @JsonProperty("endDate")
        @NotNull(message = "종료일은 필수입니다")
        OffsetDateTime endDate,

        @JsonProperty("productId")
        @NotNull(message = "상품 ID는 필수입니다")
        UUID productId
) {
    public GroupPurchaseRegisterCommand toCommand() {
        return new GroupPurchaseRegisterCommand(minQuantity, maxQuantity, title, description, discountedPrice, startDate, endDate, productId);
    }
}

