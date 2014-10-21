package org.neo4j.ogm.strategy.simple;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.dictionary.AttributeDictionary;
import org.neo4j.ogm.metadata.dictionary.FieldDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SimpleFieldDictionary extends FieldDictionary implements AttributeDictionary {

    @Override
    protected Field findScalarField(Object instance, Object parameter, String property) throws MappingException {

        for (Field field: declaredFieldsOn(instance.getClass())) {
            if (field.getName().equals(property)) {
                Type type = field.getGenericType();
                Class clazz = parameter.getClass();
                if (type.equals(clazz) || type.equals(ClassUtils.unbox(clazz))) {
                    return field;
                }
            }
        }
        throw new MappingException("Could not find field: " + property);
    }

    @Override
    protected Field findCollectionField(Object instance, Object parameter, Class elementType, String property) throws MappingException {

        Class<?> clazz = instance.getClass();
        for (Field field : declaredFieldsOn(clazz)) {
            if (field.getName().startsWith(property)) {

                if (field.getType().isArray()) {
                    Object arrayType = ((Iterable<?>)parameter).iterator().next();
                    if ((arrayType.getClass().getSimpleName() + "[]").equals(field.getType().getSimpleName())) {
                        return field;
                    }
                    if (ClassUtils.primitiveArrayName(elementType).equals(field.getType().getName())) {
                        return field;
                    }

                }
                else if (field.getType().getTypeParameters().length > 0) {
                    Class<?> returnType;
                    try {
                        returnType = Class.forName(field.getType().getName());
                    } catch (Exception e) {
                        throw new MappingException(e.getLocalizedMessage());
                    }
                    if (returnType.isAssignableFrom(parameter.getClass())) {  // the best we can do with type erasure
                        return field;
                    }
                }
            }
        }

        throw new MappingException("Could not find collection or array field: " + property);
    }

    @Override
    public Set<String> lookUpCompositeEntityAttributesFromType(Class<?> typeToPersist) {
        Set<String> compositeEntityAttributes = new HashSet<>();
        Set<String> valueAttributes = lookUpValueAttributesFromType(typeToPersist);

        // assumes all fields that aren't mappable to properties are entities
        for (Field field : declaredFieldsOn(typeToPersist)) {
            if (!valueAttributes.contains(field.getName())) {
                compositeEntityAttributes.add(field.getName());
            }
        }
        return compositeEntityAttributes;
    }

    @Override
    public Set<String> lookUpValueAttributesFromType(Class<?> typeToPersist) {
        Set<String> valueAttributes = new HashSet<>();
        for (Field field : declaredFieldsOn(typeToPersist)) {
            Class<?> fieldType = field.getType();
            // XXX: should this actually be a method on ClassInfo?
            if (fieldType.isArray() || ClassUtils.unbox(fieldType).isPrimitive() || String.class.equals(fieldType)) {
                valueAttributes.add(field.getName());
            }
        }
        return valueAttributes;
    }

    @Override
    public String lookUpRelationshipTypeForAttribute(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "HAS_" + attributeName.toUpperCase();
    }

    @Override
    public String lookUpPropertyNameForAttribute(String attributeName) {
        // simple strategy assumes that the node/relationship property name will match the object attribute name
        return attributeName;
    }

    /**
     * Retrieves all declared fields on the specified type and its superclasses, if any.
     *
     * @param type The {@link Class} from which to elicit the fields
     * @return The {@link Field}s on the specified class and its superclasses
     */
    private static Iterable<Field> declaredFieldsOn(Class<?> type) {
        return collectDeclaredFields(new HashSet<Field>(), type);
    }

    private static Collection<Field> collectDeclaredFields(Collection<Field> fields, Class<?> type) {
        for (Field declaredField : type.getDeclaredFields()) {
            fields.add(declaredField);
        }
        if (type.getSuperclass() != null) {
            collectDeclaredFields(fields, type.getSuperclass());
        }
        return fields;
    }

}
