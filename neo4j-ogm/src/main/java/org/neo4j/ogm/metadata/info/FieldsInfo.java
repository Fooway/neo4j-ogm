package org.neo4j.ogm.metadata.info;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class FieldsInfo {

    private Map<String, FieldInfo> fields = new HashMap<>();

    FieldsInfo() {}

    public FieldsInfo(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        // get the field information for this class
        int fieldCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            dataInputStream.skipBytes(2); // access_flags
            String fieldName = constantPool.lookup(dataInputStream.readUnsignedShort()); // name_index
            String descriptor = constantPool.lookup(dataInputStream.readUnsignedShort()); // descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            ObjectAnnotations objectAnnotations = new ObjectAnnotations();
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
            fields.put(fieldName, new FieldInfo(fieldName, descriptor, objectAnnotations));
        }
    }

    public Collection<FieldInfo> fields() {
        return fields.values();
    }

    public FieldInfo get(String name) {
        return fields.get(name);
    }

    public void append(FieldsInfo fieldsInfo) {
        for (FieldInfo fieldInfo : fieldsInfo.fields()) {
            if (!fields.containsKey(fieldInfo.getName())) {
                fields.put(fieldInfo.getName(), fieldInfo);
            }
        }
    }
}
