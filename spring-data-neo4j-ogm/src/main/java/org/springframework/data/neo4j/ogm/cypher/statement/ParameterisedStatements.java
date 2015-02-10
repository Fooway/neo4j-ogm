package org.springframework.data.neo4j.ogm.cypher.statement;

import java.util.List;

public class ParameterisedStatements {

    private final List<ParameterisedStatement> statements;

    public ParameterisedStatements(List<ParameterisedStatement> statements) {
        this.statements = statements;
    }

    public List<ParameterisedStatement> getStatements() {
        return statements;
    }

}
