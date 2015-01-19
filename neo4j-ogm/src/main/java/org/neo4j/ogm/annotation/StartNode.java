package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a domain entity as being the start node of
 * a relationship in the graph, and, along with @EndNode
 * is a mandatory annotation on any domain entity that is annotated
 * with @RelationshipEntity
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface StartNode {
    static final String CLASS = "org.neo4j.ogm.annotation.StartNode";
}
