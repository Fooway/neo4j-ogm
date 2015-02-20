/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;

public class FieldReader implements RelationalReader, PropertyReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;

    FieldReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    @Override
    public Object read(Object instance) {
        Object value = FieldWriter.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasConverter()) {
            value = fieldInfo.converter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public String relationshipType() {
        return fieldInfo.relationship();
    }

    @Override
    public String propertyName() {
        return fieldInfo.property();
    }

    @Override
    public String relationshipDirection() {
        try {
            return fieldInfo.getAnnotations().get(Relationship.CLASS).get(Relationship.DIRECTION, Relationship.OUTGOING);
        } catch (NullPointerException npe) {
            return Relationship.OUTGOING;
        }
    }

}
