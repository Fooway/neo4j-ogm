package org.springframework.data.neo4j.ogm.domain.rulers;

public class Emperor extends Person implements Ruler {

    @Override
    public String rulesOver() {
        return "Empire";
    }

    @Override
    public String sex() {
        return "Male";
    }

    @Override
    public boolean isCommoner() {
        return false;
    }
}
