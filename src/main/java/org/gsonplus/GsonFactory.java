package org.gsonplus;

import com.google.gson.Gson;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import org.gsonplus.proxy.TypeAdapterFactoryProxy;
import org.gsonplus.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liujun
 */
public class GsonFactory {
    public static Gson build(boolean ignoreSerializedName, boolean ignoreExclude) {
        if (!ignoreSerializedName && ignoreExclude) {
            return new Gson();
        }

        Gson gson = new Gson();

        Field field = ReflectionUtils.findField(Gson.class, "factories");
        ReflectionUtils.makeAccessible(field);
        List<TypeAdapterFactory> factories = (List<TypeAdapterFactory>) ReflectionUtils.getField(field, gson);

        List<TypeAdapterFactory> list = new ArrayList<TypeAdapterFactory>(factories);
        TypeAdapterFactory target = null;
        int index;
        for (index = 0; index < list.size(); index++) {
            if (list.get(index) instanceof ReflectiveTypeAdapterFactory) {
                target = list.get(index);
                break;
            }
        }

        TypeAdapterFactory proxy = null;
        if (target != null) {
            proxy = (TypeAdapterFactory) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(), new TypeAdapterFactoryProxy(target, ignoreSerializedName, ignoreExclude));
            list.set(index, proxy);
//            Field c = ReflectionUtils.findField(factories.getClass().getSuperclass().getSuperclass(),  "c");
//            ReflectionUtils.makeAccessible(c);
            ReflectionUtils.setField(field, gson, list);
        }
        return gson;
    }
}
