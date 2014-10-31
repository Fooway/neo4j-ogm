package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Method;

public class MethodAccess extends ObjectAccess {

    public static void write(Method method, Object instance, Object value) {
        try {
            Class parameterType = method.getParameterTypes()[0];
            System.out.println("method parameter type: " + method.getParameterTypes()[0]);
            System.out.println("value type: " + value.getClass());

            // TODO: this needs to move elsewhere because read won't work with this method, and there may be no getter!
            if (Iterable.class.isAssignableFrom(parameterType) || parameterType.isArray()) {
                value = merge(method.getParameterTypes()[0], (Iterable<?>) value, (Iterable<?>) read(method, instance));
            }

            method.invoke(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object read(Method method, Object instance) {
//        try {
//            return method.invoke(instance);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        return null;
    }
}
