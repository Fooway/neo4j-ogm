package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodsInfo {

    private Map<String, MethodInfo> methods = new HashMap<>();
    private Map<String, MethodInfo> getters = new HashMap<>();
    private Map<String, MethodInfo> setters = new HashMap<>();

    MethodsInfo() {}

    public MethodsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the method information for this class
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String methodName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.lookup(dataInputStream.readUnsignedShort()); // descriptor
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
            int attributesCount = dataInputStream.readUnsignedShort();
            for (int j = 0; j < attributesCount; j++) {
                String attributeName = constantPool.lookup(dataInputStream.readUnsignedShort());
                int attributeLength = dataInputStream.readInt();
                if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                    int annotationCount = dataInputStream.readUnsignedShort();
                    for (int m = 0; m < annotationCount; m++) {
                        AnnotationInfo info = new AnnotationInfo(dataInputStream, constantPool);
                        // todo: maybe register just the annotations we're interested in.
                        objectAnnotations.put(info.getName(), info);
                    }
                }
                else {
                    dataInputStream.skipBytes(attributeLength);
                }
            }
            if (!methodName.equals("<init>")) {
                addMethod(new MethodInfo(methodName, descriptor, objectAnnotations));
            }
        }
    }

    public Collection<MethodInfo> methods() {
        return methods.values();
    }

    public Collection<MethodInfo> getters() {
        return getters.values();
    }

    public Collection<MethodInfo> setters() {
        return setters.values();
    }

    public MethodInfo get(String methodName) {
        return methods.get(methodName);
    }

    public void append(MethodsInfo methodsInfo) {
        for (MethodInfo methodInfo : methodsInfo.methods()) {
            if (!methods.containsKey(methodInfo.getName())) {
                addMethod(methodInfo);
            }
        }
    }

    private void addMethod(MethodInfo methodInfo) {
        String methodName = methodInfo.getName();
        String descriptor = methodInfo.getDescriptor();
        methods.put(methodName, methodInfo);
        if (methodName.startsWith("get") && descriptor.startsWith("()")) {
            getters.put(methodName, methodInfo);
        }
        else if (methodName.startsWith("set") && descriptor.endsWith(")V")) {
            setters.put(methodName, methodInfo);
        }
    }

}
