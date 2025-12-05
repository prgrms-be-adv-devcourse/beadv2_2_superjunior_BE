package store._0982.product.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 404 Not Found
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    GROUPPURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "공동구매를 찾을 수 없습니다."),

    // 403 Forbidden
    NON_SELLER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다. 판매자 권한이 필요합니다."),
    FORBIDDEN_NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "본인이 등록한 상품만 삭제할 수 있습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
