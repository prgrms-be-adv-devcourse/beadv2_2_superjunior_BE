package store_0982.dummy_data.util;

import store._0982.member.domain.member.Member;

import java.lang.reflect.Field;

public class Utils {
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = Member.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field " + fieldName, e);
        }
    }
}
