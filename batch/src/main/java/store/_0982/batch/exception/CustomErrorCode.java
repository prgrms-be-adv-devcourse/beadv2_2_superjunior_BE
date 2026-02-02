package store._0982.batch.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {

    //400 Bad Request
    INVALID_ACCOUNT_INFO(HttpStatus.BAD_REQUEST, "유효하지 않은 계좌 정보입니다."),

    // 404 Not Found
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "판매자를 찾을 수 없습니다."),

    // 409 Conflict
    DB_ES_COUNT_MISMATCH(HttpStatus.CONFLICT, "DB와 ES의 갯수가 다릅니다."),

    // 500 Internal Server Error
    ES_BULK_INSERT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ES를 벌크로 재색인 하는 중 오류가 발생하였습니다."),
    ES_REINDEX_RETRY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ES 재색인을 실패한 문서가 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
