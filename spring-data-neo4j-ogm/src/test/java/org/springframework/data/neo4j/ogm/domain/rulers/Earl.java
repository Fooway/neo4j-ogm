package org.springframework.data.neo4j.ogm.domain.rulers;

public class Earl extends Nobleman {
    @Override
    public String rulesOver() {
        return "County";
    }
}
