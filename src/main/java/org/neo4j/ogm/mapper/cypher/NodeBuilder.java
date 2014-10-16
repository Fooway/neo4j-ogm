package org.neo4j.ogm.mapper.cypher;

/**
 * I don't really want another representation of Node but this'll do for now, just while we explore the API.
 */
public interface NodeBuilder {

    void addLabel(String labelName);

    void addProperty(String propertyName, Object value);

}
