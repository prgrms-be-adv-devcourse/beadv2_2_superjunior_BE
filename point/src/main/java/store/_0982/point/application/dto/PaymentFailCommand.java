package store._0982.point.application.dto;

import java.util.UUID;

/**
 * 결제 실패 정보를 저장하기 위한 명령 DTO.
 */
public record PaymentFailCommand(
        UUID orderId,
        String paymentKey,
        String errorCode,
        String errorMessage,
        Long amount,
        String rawPayload
) {
}
