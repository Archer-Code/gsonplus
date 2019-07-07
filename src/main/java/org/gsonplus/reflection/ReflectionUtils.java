package org.gsonplus.reflection;

import com.sun.istack.internal.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static sun.reflect.misc.FieldUtil.getDeclaredFields;

/**
 * @author liujun
 */
public class ReflectionUtils {
    public static Field findField(Class<?> clazz, String name) {
        for(Class searchType = clazz; Object.class != searchType && searchType != null; searchType = searchType.getSuperclass()) {
            Field[] fields = getDeclaredFields(searchType);

            for (Field field : fields) {
                if ((name == null || name.equals(field.getName()))) {
                    return field;
                }
            }
        }

        return null;
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers())
                || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
                || Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected reflection exception - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void setField(Field field, @Nullable Object target, @Nullable Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected reflection exception - " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
