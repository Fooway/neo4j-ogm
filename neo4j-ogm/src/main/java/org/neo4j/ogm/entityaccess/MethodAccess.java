package org.neo4j.ogm.entityaccess;

import java.lang.reflect.Method;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

public class MethodAccess extends ObjectAccess {

    private final ClassInfo classInfo;
    private final MethodInfo methodInfo;

    public MethodAccess(ClassInfo enclosingClassInfo, MethodInfo methodInfo) {
        this.classInfo = enclosingClassInfo;
        this.methodInfo = methodInfo;
    }

    public static void write(Method method, Object instance, Object value) {
        try {
            Class<?> parameterType = method.getParameterTypes()[0];

            // TODO: this needs to move elsewhere because read won't work with this method, and there may be no getter!
            if (Iterable.class.isAssignableFrom(parameterType) || parameterType.isArray()) {
                String getterName = method.getName().replace("set", "get");
                Method getter = instance.getClass().getDeclaredMethod(getterName);
                value = merge(method.getParameterTypes()[0], (Iterable<?>) value, (Iterable<?>) read(getter, instance));
            }

            method.invoke(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object read(Method method, Object instance) {
        try {
            return method.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        MethodAccess.write(classInfo.getMethod(methodInfo, ClassUtils.getType(methodInfo.getDescriptor())), instance, value);
    }

    @Override
    public Object read(Object instance) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

    @Override
    public String relationshipType() {
        return this.methodInfo.relationship().substring(3);
    }

}
