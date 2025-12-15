package store._0982.point.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import store._0982.common.exception.CustomException;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderInfo(
        UUID orderId,
        int price,
        Status status,
        UUID memberId
) {
    public void validateReturnable(UUID memberId, UUID orderId, long amount) {
        validate(memberId, orderId, amount);
        if (status != Status.IN_PROGRESS) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    public void validateDeductible(UUID memberId, UUID orderId, long amount) {
        validate(memberId, orderId, amount);
        if (status != Status.SUCCESS) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    private void validate(UUID memberId, UUID orderId, long amount) {
        if (this.memberId != memberId || this.orderId != orderId || price != amount) {
            throw new CustomException(CustomErrorCode.INVALID_POINT_REQUEST);
        }
    }

    public enum Status {
        SCHEDULED,      // 시작전
        IN_PROGRESS,    // 진행중
        SUCCESS,        // 완료 - 성공
        FAILED,         // 완료 - 실패
    }
}
