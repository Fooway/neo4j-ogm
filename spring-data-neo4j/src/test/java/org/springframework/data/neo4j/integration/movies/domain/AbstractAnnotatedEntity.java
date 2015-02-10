package org.springframework.data.neo4j.integration.movies.domain;

import org.springframework.data.neo4j.ogm.annotation.GraphId;
import org.springframework.data.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class AbstractAnnotatedEntity {

    @GraphId
    Long nodeId;
}
