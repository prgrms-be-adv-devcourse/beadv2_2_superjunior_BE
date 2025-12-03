package store._0982.product.common.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseEntity<T> {
    private final int status;
    private final T data;
    private final String message;

    public ResponseEntity(HttpStatus httpStatus, T all, String message) {
        this.status = httpStatus.value();
        this.data = all;
        this.message = message;
    }
}
