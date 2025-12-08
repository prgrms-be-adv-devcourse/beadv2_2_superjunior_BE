package store._0982.elasticsearch.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
// TODO: 에러 메시지 수정 필요
public enum CustomErrorCode {

    // 400 Bad Request


    // 401 Unauthorized

    // 403 Forbidden

    // 404 Not Found

    // 409 Conflict

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    // 502 Bad Gateway

    // 503 Service Unavailable

    // 504

    private final HttpStatus httpStatus;
    private final String message;
}
