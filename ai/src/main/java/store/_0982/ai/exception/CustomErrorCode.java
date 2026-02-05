package store._0982.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import store._0982.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CustomErrorCode implements ErrorCode {
    //400 BAD_REQUEST
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
