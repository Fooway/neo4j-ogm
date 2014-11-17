package org.neo4j.ogm.unit.mapper.cypher;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;

import static org.junit.Assert.assertEquals;

public class CypherStatementsTest {

    CypherStatements cypherStatements;
    @Before
    public void setUp() throws Exception {
        cypherStatements = new CypherStatements();
    }

    @Test
    public void testStatement() throws Exception {
        cypherStatements.add(new DepthOneStrategy().findOne(123L));
        assertEquals("{\"statements\" : [ { \"statement\": \"MATCH p=(n)--(m) WHERE id(n) = 123 RETURN p\", \"resultDataContents\" : [ \"graph\" ] } ] }", cypherStatements.toString());

    }

}
