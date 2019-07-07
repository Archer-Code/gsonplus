package org.gsonplus.proxy;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.gsonplus.annotation.Exclude;
import org.gsonplus.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liujun
 */
public class TypeAdapterFactoryProxy implements InvocationHandler {
    private TypeAdapterFactory target;

    private final boolean ignoreSerializedName;

    private final boolean ignoreExclude;

    public TypeAdapterFactoryProxy(TypeAdapterFactory target, boolean ignoreSerializedName, boolean ignoreExclude) {
        this.target = target;
        this.ignoreSerializedName = ignoreSerializedName;
        this.ignoreExclude = ignoreExclude;
    }

    public TypeAdapterFactoryProxy(TypeAdapterFactory target) {
        this(target, true, false);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TypeAdapter typeAdapter = (TypeAdapter) method.invoke(target, args);
        TypeToken typeToken = (TypeToken) args[1];
        Class rawType = typeToken.getRawType();
        if (rawType.isPrimitive() || rawType.isAssignableFrom(String.class)
                || rawType.isAssignableFrom(Number.class)) {
            return typeAdapter;
        }
        Map<String, BoundFieldInner> fieldMap = getAnnotatedField(rawType);
        if (null != fieldMap || fieldMap.size() > 0) {
            procField(typeAdapter, typeToken, rawType, fieldMap);
        }
        return typeAdapter;
    }

    private Map<String, BoundFieldInner> getAnnotatedField(Class rawType) {
        Field[] fields = rawType.getDeclaredFields();
        Map<String, BoundFieldInner> map = new HashMap<String, BoundFieldInner>(fields.length > 0 ? fields.length : 1);
        BoundFieldInner boundFieldInner;
        for (Field field : fields) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            Exclude exclude = field.getAnnotation(Exclude.class);
            if (null == serializedName && null == exclude) {
                continue;
            }
            boundFieldInner = new BoundFieldInner();
            if (null != exclude && !ignoreExclude) {
                boundFieldInner.serialized = false;
            }

            if (null != serializedName) {
                boundFieldInner.name = serializedName.value();
            } else {
                boundFieldInner.name = field.getName();
            }
            boundFieldInner.originalName = field.getName();
            map.put(boundFieldInner.name, boundFieldInner);
        }
        return map;
    }

    private void procField(TypeAdapter typeAdapter, TypeToken typeToken, Class rawType, Map<String, BoundFieldInner> fieldMap) {
        Field field = ReflectionUtils.findField(typeAdapter.getClass(), "boundFields");
        ReflectionUtils.makeAccessible(field);
        Map boundFiledMap = (Map) ReflectionUtils.getField(field, typeAdapter);
        Object value;
        BoundFieldInner boundFieldInner;
        String serializedName;
        for (String name : fieldMap.keySet()) {
            value = boundFiledMap.remove(name);
            if (null != value) {
                boundFieldInner = fieldMap.get(name);
                if (null != boundFieldInner && boundFieldInner.serialized) {
                    if (ignoreSerializedName) {
                        serializedName = boundFieldInner.originalName;
                        setField("name", value, serializedName);
                    } else {
                        serializedName = boundFieldInner.name;
                    }
                    boundFiledMap.put(serializedName, value);
                }
            }
        }
    }

    private void setField(String fieldName, Object target, Object value) {
        Field field = ReflectionUtils.findField(target.getClass(), fieldName);
        ReflectionUtils.setField(field, target, value);
    }

    class BoundFieldInner {
        String name = null;
        boolean serialized;
        String originalName;
    }
}
