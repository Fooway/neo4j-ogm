package org.springframework.data.neo4j.ogm.domain.forum;

import org.springframework.data.neo4j.ogm.annotation.Property;

// todo: default label and another in the class hierarchy...
//@Label
public abstract class Membership implements IMembership {

    @Property(name="annualFees")
    private Integer fees;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFees() {
        return fees;
    }

    public void setFees(Integer fees) {
        this.fees = fees;
    }

}
