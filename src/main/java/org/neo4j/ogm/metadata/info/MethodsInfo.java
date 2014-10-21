package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MethodsInfo {

    private Map<String, ObjectAnnotations> methodsInfoMap = new HashMap<>();

    MethodsInfo() {}

    public MethodsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the method information for this class
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String methodName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            dataInputStream.skipBytes(2); // descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            for (int j = 0; j < attributesCount; j++) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
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
                methodsInfoMap.put(methodName, objectAnnotations);
            }
        }
    }

    public ObjectAnnotations getAnnotations(String fieldName) {
        return methodsInfoMap.get(fieldName);
    }
}
