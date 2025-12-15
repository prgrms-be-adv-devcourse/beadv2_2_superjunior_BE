package store._0982.order.presentation.order.dto;

import jakarta.validation.constraints.*;
import store._0982.order.application.order.dto.OrderRegisterCommand;

import java.util.UUID;

public record OrderRegisterRequest(
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        int quantity,
        @NotBlank(message = "주소는 필수입니다")
        @Size(min = 5, max = 100, message = "주소는 5자 이상 100자 이하여야 합니다")
        String address,
        @NotBlank(message = "상세 주소는 필수입니다")
        @Size(min = 1, max = 100, message = "상세 주소는 1자 이상 100자 이하여야 합니다")
        String addressDetail,
        @NotBlank(message = "우편번호는 필수입니다")
        String postalCode,
        @Size(max = 100, message = "수신자 이름은 100자 이하여야 합니다")
        String receiverName,
        @NotNull(message = "판매자 ID는 필수입니다.")
        UUID sellerId,
        @NotNull(message = "공동 구매 ID는 필수입니다.")
        UUID groupPurchaseId
) {
    public OrderRegisterCommand toCommand(){
        return new OrderRegisterCommand(quantity, address, addressDetail, postalCode, receiverName, sellerId, groupPurchaseId);
    }
}
