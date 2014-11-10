package org.neo4j.ogm.mapper;

import java.util.List;

/**
 * Specification for an object-graph mapper, which can arbitrary Java objects onto Cypher data manipulation queries.
 */
public interface ObjectToCypherMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to
     * persist their state in Neo4j.
     *
     * @param toPersist The "root" node of the object graph to persist
     * @return A {@link List} of Cypher queries to write the data contained within the given object to Neo4j
     * @throws NullPointerException if invoked with <code>null</code>
     */
    List<String> mapToCypher(Object toPersist);

}
