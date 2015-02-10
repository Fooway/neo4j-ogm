package org.springframework.data.neo4j.ogm.domain.satellites;

import org.springframework.data.neo4j.ogm.annotation.Property;

/**
 * This object is entirely hydrated via its fields
 */
public class Location extends DomainObject {

    @Property(name="location")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
