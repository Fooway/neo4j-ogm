package org.neo4j.ogm.mapper.cypher;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.session.CypherQuery;

import static org.junit.Assert.assertEquals;

public class CypherStatementsTest {

    CypherStatements cypherStatements;
    @Before
    public void setUp() throws Exception {
        cypherStatements = new CypherStatements();
    }

    @Test
    public void testOneStatement() throws Exception {
        cypherStatements.add(new CypherQuery().findOne(123L));
        assertEquals("{\"statements\" : [ { \"statement\": \"MATCH p=(n)-->(m) WHERE id(n) = 123 RETURN p;\" } ] }", cypherStatements.toString());

    }

    @Test
    public void testMultipleStatements() throws Exception {
        cypherStatements.add(new CypherQuery().findOne(123L))
                        .add(new CypherQuery().findOne(345L));
        assertEquals("{\"statements\" : [ { \"statement\": \"MATCH p=(n)-->(m) WHERE id(n) = 123 RETURN p;\" }, { \"statement\": \"MATCH p=(n)-->(m) WHERE id(n) = 345 RETURN p;\" } ] }", cypherStatements.toString());

    }

}
