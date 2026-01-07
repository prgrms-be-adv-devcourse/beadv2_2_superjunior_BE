package store._0982.elasticsearch.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {

    // 400 Bad Request
    SELLER_ID_ISNULL(HttpStatus.BAD_REQUEST,"판매자 ID가 비어있습니다."),

    // 401 Unauthorized

    // 403 Forbidden

    // 404 Not Found
    DONOT_EXIST_INDEX(HttpStatus.NOT_FOUND, "삭제할 인덱스가 존재하지 않습니다."),

    // 409 Conflict
    ALREADY_EXIST_INDEX(HttpStatus.CONFLICT, "이미 존재하는 인덱스입니다."),

    // 500 Internal Server Error
    REINDEX_COUNT_MISMATCH(HttpStatus.INTERNAL_SERVER_ERROR, "재색인 결과 수가 일치하지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
