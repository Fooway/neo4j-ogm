package org.neo4j.ogm.mapper;

import org.neo4j.ogm.entityaccess.FieldAccess;
import org.neo4j.ogm.mapper.cypher.CypherBuilder;
import org.neo4j.ogm.mapper.cypher.CypherBuildingContext;
import org.neo4j.ogm.mapper.cypher.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatements;
import org.neo4j.ogm.mapper.cypher.single.SingleQueryCypherBuilder;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.session.MappedRelationshipCache;
import org.neo4j.ogm.session.MappedRelationship;

/**
 * Implementation of {@link ObjectToCypherMapper} that is driven by an instance of {@link MetaData}.
 */
public class MetaDataDrivenObjectToCypherMapper implements ObjectToCypherMapper {

    private final MetaData metaData;
    private final MappedRelationshipCache mappedRelationships;

    /**
     * Constructs a new {@link MetaDataDrivenObjectToCypherMapper} that uses the given {@link MetaData}.
     *
     * @param metaData The {@link MetaData} containing the mapping information
     * @param mappedRelationships The {@link MappedRelationshipCache} containing the relationships loaded in the current session
     */
    public MetaDataDrivenObjectToCypherMapper(MetaData metaData, MappedRelationshipCache mappedRelationships) {
        this.metaData = metaData;
        this.mappedRelationships = mappedRelationships;
    }

    @Override
    public ParameterisedStatements mapToCypher(Object toPersist) {
        if (toPersist == null) {
            throw new NullPointerException("Cannot map null root object");
        }

        CypherBuilder cypherBuilder = new SingleQueryCypherBuilder();
        CypherBuildingContext context = new CypherBuildingContext();
        deepMap(cypherBuilder, toPersist, context);
        deleteObsoleteRelationships(cypherBuilder, context);

        return new ParameterisedStatements(cypherBuilder.getStatements());
    }

    private void deleteObsoleteRelationships(CypherBuilder cypherBuilder, CypherBuildingContext context) {
        for (MappedRelationship rel : this.mappedRelationships) {
            // TODO: optimise this, it does too many "findById" calls throughout this process - suggest some sort of result object
            // it also feels like I'm calling getThis getThat a few too many times
            if (context.doesNotContainRelationship(rel.getStartNodeId(), rel.getRelationshipType(), rel.getEndNodeId())) {
                NodeBuilder startNode = context.findById(rel.getStartNodeId());
                if (startNode == null) {
                    startNode = cypherBuilder.existingNode(rel.getStartNodeId());
                }
                NodeBuilder endNode = context.findById(rel.getEndNodeId());
                if (endNode == null) {
                    endNode = cypherBuilder.existingNode(rel.getEndNodeId());
                }
                cypherBuilder.unrelate(startNode, rel.getRelationshipType(), endNode);
            }
        }
    }

    /**
     * Builds Cypher to save the specified object and all its composite objects into the graph database.
     *
     * @param cypherBuilder The builder used to construct the query
     * @param toPersist The object to persist into the graph database
     * @param context A {@link CypherBuildingContext} that manages the objects visited during the mapping process
     * @return The "root" node of the object graph that matches
     */
    private NodeBuilder deepMap(CypherBuilder cypherBuilder, Object toPersist, CypherBuildingContext context) {
        if (context.containsObject(toPersist)) {
            return context.retrieveNodeBuilderForObject(toPersist);
        }
        NodeBuilder nodeBuilder = buildNode(cypherBuilder, toPersist, context);
        mapPropertyFieldsToNodeProperties(toPersist, nodeBuilder);
        mapNestedEntitiesToGraphObjects(cypherBuilder, toPersist, nodeBuilder, context);
        return nodeBuilder;
    }

    private NodeBuilder buildNode(CypherBuilder cypherBuilder, Object toPersist, CypherBuildingContext context) {
        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());
        Object id = FieldAccess.read(classInfo.getField(classInfo.identityField()), toPersist);
        if (id == null) {
            NodeBuilder newNode = cypherBuilder.newNode().addLabels(classInfo.labels());
            context.add(toPersist, newNode);
            return newNode;
        }
        NodeBuilder existingNode = cypherBuilder.existingNode(Long.valueOf(id.toString())).addLabels(classInfo.labels());
        context.add(toPersist, Long.valueOf(id.toString()), existingNode);
        return existingNode;
    }

    private void mapPropertyFieldsToNodeProperties(Object toPersist, NodeBuilder nodeBuilder) {
        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());

        // TODO this is field-specific - need to add support for method-based writing
        for (FieldInfo propertyField : classInfo.propertyFields()) {
            String propertyName = propertyField.property();
            Object value = FieldAccess.read(classInfo.getField(propertyField), toPersist);
            nodeBuilder.addProperty(propertyName, value);
        }
    }

    private void mapNestedEntitiesToGraphObjects(CypherBuilder cypherBuilder, Object toPersist, NodeBuilder nodeBuilder,
            CypherBuildingContext context) {
        ClassInfo classInfo = metaData.classInfo(toPersist.getClass().getName());
        // TODO again, this is field-specific
        for (FieldInfo relField : classInfo.relationshipFields()) {
            Object nestedEntity = FieldAccess.read(classInfo.getField(relField), toPersist);
            String relationshipType = resolveRelationshipType(relField);
            if (nestedEntity instanceof Iterable) {
                // create a relationship for each of these nested entities
                for (Object object : (Iterable<?>) nestedEntity) {
                    NodeBuilder newNode = deepMap(cypherBuilder, object, context);
                    cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
                    context.registerRelationship(nodeBuilder, relationshipType, newNode);
                }
            } else {
                if (nestedEntity != null && !context.containsObject(toPersist)) {
                    // TODO: assuming outbound relationship, need to consider what the annotation says
                    NodeBuilder newNode = deepMap(cypherBuilder, nestedEntity, context);
                    cypherBuilder.relate(nodeBuilder, relationshipType, newNode);
                    context.registerRelationship(nodeBuilder, relationshipType, newNode);
                }
            }
        }
    }

    private static String resolveRelationshipType(FieldInfo relField) {
        // should be doing the opposite of ObjectGraphMapper#setterNameFromRelationshipType, but I don't know at this point
        // whether or not the relationship is read from an annotation and therefore don't know if I shouldn't do any work!

        // @Adam: the relationship() method will return the relationshipType name if its annotated on the relationShipInfo. If not,
        // it currently returns the field type name, e.g. "Wheel", which is not what we ultimately want.

        // todo: fix this for non-annotated fields (and getters)
        return relField.relationship().toUpperCase();
    }

}
