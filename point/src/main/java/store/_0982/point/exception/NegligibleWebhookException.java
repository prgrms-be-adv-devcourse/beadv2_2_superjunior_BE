package store._0982.point.exception;

import lombok.Getter;

/**
 * 웹훅 재시도가 불필요한 검증 실패 에러
 *
 * <p>PG API가 HTTP 상태 코드(4xx/5xx)를 구분하지 않고 모든 에러에 재시도하므로, (토스 등) <br>
 * 재시도가 의미 없는 경우(비즈니스 검증 실패 등) 200 응답을 반환하여
 * 무한 재시도를 방지합니다.</p>
 *
 * <p>주의: 정상 처리가 아니므로 반드시 ERROR 레벨로 로깅해야 합니다.</p>
 */
@Getter
public class NegligibleWebhookException extends RuntimeException {

    private final NegligibleWebhookErrorType errorType;

    public NegligibleWebhookException(NegligibleWebhookErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
}
