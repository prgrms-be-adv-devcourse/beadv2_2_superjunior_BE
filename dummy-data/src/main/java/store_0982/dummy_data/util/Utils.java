package store_0982.dummy_data.util;

import java.lang.reflect.Field;

public class Utils {
    public static <T> void setField(T target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field " + fieldName, e);
        }
    }
}
