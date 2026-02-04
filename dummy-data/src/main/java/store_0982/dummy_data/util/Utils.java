package store_0982.dummy_data.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

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

    public static <T> String makeCsvHeaderString(Class<T> clazz, List<String> excluded) {
        String camelCase =  String.join("," ,Stream.of(clazz.getDeclaredFields()).map(Field::getName).filter(name -> !excluded.contains(name)).toList()) + "\n";
        return toSnakeCase(camelCase);
    }

    private static String toSnakeCase(String camelCase) {
        StringBuilder sb = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static <T> String makeCsvRowString(T entity, List<String> excluded) throws IllegalAccessException {
        List<Field> fields = Stream.of(entity.getClass().getDeclaredFields())
                .filter(f -> !excluded.contains(f.getName()))
                .toList();
        StringBuilder row = new StringBuilder();
        for (Field field : fields) {
            if (!row.isEmpty()) {
                row.append(',');
            }
            field.setAccessible(true);
            Object value = field.get(entity);
            row.append(value != null ? value : "");
        }
        row.append('\n');
        return row.toString();
    }


}
