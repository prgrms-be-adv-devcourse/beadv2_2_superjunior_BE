package store._0982.point.exception;

import lombok.Getter;
import store._0982.common.exception.CustomException;

@Getter
public class EntityNotFoundException extends CustomException {

    public EntityNotFoundException(CustomErrorCode errorCode) {
        super(errorCode);
    }
}
