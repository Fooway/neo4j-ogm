package org.neo4j.ogm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Relationship {

    static final String CLASS = "org.neo4j.ogm.annotation.Relationship";
    static final String TYPE = "type";
    static final String DIRECTION = "direction";

    static final String INCOMING = "INCOMING";
    static final String OUTGOING = "OUTGOING";

    String type() default "";
    String direction() default OUTGOING; // todo use neo4j direction enum (MB: really? I wouldn't introduce that dependency just for directions)
}
