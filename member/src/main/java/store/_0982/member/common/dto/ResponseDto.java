package store._0982.member.common.dto;

import org.springframework.http.HttpStatus;

public record ResponseDto<T>(int status, T data, String message) {
    public ResponseDto(HttpStatus status, T data, String message) {
        this(status.value(), data, message);
    }
}
