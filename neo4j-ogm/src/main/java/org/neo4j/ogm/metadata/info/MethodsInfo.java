/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.metadata.info;

import org.neo4j.ogm.annotation.Transient;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MethodsInfo {

    private final Map<String, MethodInfo> methods = new HashMap<>();
    private final Map<String, MethodInfo> getters = new HashMap<>();
    private final Map<String, MethodInfo> setters = new HashMap<>();

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
            String typeParameterDescriptor = null; // available as an attribute for parameterised collections
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
                } else if ("Signature".equals(attributeName)) {
                    String signature = constantPool.lookup(dataInputStream.readUnsignedShort());
                    if (signature.contains("<")) {
                        typeParameterDescriptor = signature.substring(signature.indexOf('<') + 1, signature.indexOf('>'));
                    }
                } else {
                    dataInputStream.skipBytes(attributeLength);
                }
            }
            if (!methodName.equals("<init>") && objectAnnotations.get(Transient.CLASS) == null) {
                addMethod(new MethodInfo(methodName, descriptor, typeParameterDescriptor, objectAnnotations));
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
