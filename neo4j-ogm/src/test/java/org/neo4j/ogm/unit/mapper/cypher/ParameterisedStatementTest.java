package org.neo4j.ogm.unit.mapper.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.graphmodel.Property;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParameterisedStatementTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private ParameterisedStatement statement;

    @Test
    public void testFindOne() throws Exception {
        statement = new VariableDepthQuery().findOne(123L, 1);
        assertEquals("MATCH p=(n)-[*1..1]-(m) WHERE id(n) = { id } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new VariableDepthQuery().findAll(ids, 1);
        assertEquals("MATCH p=(n)-[*1..1]-(m) WHERE id(n) in { ids } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void testFindByLabel() throws Exception {
        statement = new VariableDepthQuery().findByLabel("NODE", 1);
        assertEquals("MATCH p=(n:NODE)-[*1..1]-(m) RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findAll() throws Exception {
        statement = new VariableDepthQuery().findAll();
        assertEquals("MATCH p=()-->() RETURN p", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStringValue() throws Exception {
        statement = new VariableDepthQuery().findByProperty("Asteroid", new Property<String, Object>("ref", "45 Eugenia"), 1);
        assertEquals("MATCH p=(n:Asteroid)-[*1..1]-(m) WHERE n.ref = { ref } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"ref\":\"45 Eugenia\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyIntegralValue() throws Exception {
        statement =  new VariableDepthQuery().findByProperty("Asteroid", new Property<String, Object>("index", 77), 1);
        assertEquals("MATCH p=(n:Asteroid)-[*1..1]-(m) WHERE n.index = { index } RETURN collect(distinct p)",statement.getStatement());
        assertEquals("{\"index\":77}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyStandardForm() throws Exception {
        statement = new VariableDepthQuery().findByProperty("Asteroid", new Property<String, Object>("diameter", 6.02E1), 1);
        assertEquals("MATCH p=(n:Asteroid)-[*1..1]-(m) WHERE n.diameter = { diameter } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyDecimal() throws Exception {
        statement = new VariableDepthQuery().findByProperty("Asteroid", new Property<String, Object>("diameter", 60.2), 1);
        assertEquals("MATCH p=(n:Asteroid)-[*1..1]-(m) WHERE n.diameter = { diameter } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"diameter\":60.2}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void findByPropertyEmbeddedDelimiter() throws Exception {
        statement = new VariableDepthQuery().findByProperty("Cookbooks", new Property<String, Object>("title", "Mrs Beeton's Household Recipes"), 1);
        assertEquals("MATCH p=(n:Cookbooks)-[*1..1]-(m) WHERE n.title = { title } RETURN collect(distinct p)", statement.getStatement());
        assertEquals("{\"title\":\"Mrs Beeton's Household Recipes\"}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void delete() throws Exception {
        statement = new DeleteStatements().delete(123L);
        assertEquals("MATCH (n) WHERE id(n) = { id } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"id\":123}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        statement = new DeleteStatements().deleteAll(ids);
        assertEquals("MATCH (n) WHERE id(n) in { ids } OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{\"ids\":[123,234,345]}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        statement = new DeleteStatements().deleteByLabel("NODE");
        assertEquals("MATCH (n:NODE) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

    @Test
    public void purge() throws Exception {
        statement = new DeleteStatements().purge();
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", statement.getStatement());
        assertEquals("{}", mapper.writeValueAsString(statement.getParameters()));
    }

}
