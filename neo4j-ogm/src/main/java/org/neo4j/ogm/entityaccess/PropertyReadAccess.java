package org.neo4j.ogm.entityaccess;

/**
 * Simple interface through which a particular property of a given object can be read.
 */
public interface PropertyReadAccess {

    Object read(Object instance);

}
