package store._0982.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EntityErrorCode implements ErrorCode {

    INVALID_SETTLEMENT_AMOUNT(HttpStatus.BAD_REQUEST, "잘못된 정산 금액입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "정산 잔액이 부족합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
