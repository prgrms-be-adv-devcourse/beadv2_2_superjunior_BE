package store._0982.point.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import store._0982.common.exception.CustomException;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderInfo(
        UUID orderId,
        long price,
        Status status,
        UUID memberId,
        int quantity
) {
    public enum Status {
        PENDING,
        ORDER_FAILED,
        COMPLETED,
        CANCELLED,
        GROUP_PURCHASE_SUCCESS,
        GROUP_PURCHASE_FAILED,
        REVERSED,
        RETURNED
    }

    public void validateReturnable(UUID memberId, UUID orderId, long amount) {
        validate(memberId, orderId, amount);
        if (status != Status.ORDER_FAILED) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    public void validateDeductible(UUID memberId, UUID orderId, long amount) {
        validate(memberId, orderId, amount);
        if (status != Status.CANCELLED) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    public void validateChargeable(UUID memberId) {
        if (!this.memberId.equals(memberId)) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    public void validateAutoChargeable(UUID memberId, UUID orderId, long amount) {
        validate(memberId, orderId, amount);
        if (status != Status.PENDING) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    private void validate(UUID memberId, UUID orderId, long amount) {
        if (!this.memberId.equals(memberId) || !this.orderId.equals(orderId) || price * quantity != amount) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }
}
