package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.Property;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CypherQueryTest {

    @Test
    public void testFindOne() throws Exception {
        assertEquals("MATCH p=(n)--(m) WHERE id(n) = 123 RETURN p", new CypherQuery().findOne(123L));
    }

    @Test
    public void testFindAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH p=(n)--(m) WHERE id(n) in [123,234,345] RETURN p", new CypherQuery().findAll(ids));
    }

    @Test
    public void testFindByLabel() throws Exception {
        assertEquals("MATCH p=(n:NODE)--(m) RETURN p", new CypherQuery().findByLabel("NODE"));
    }

    @Test
    public void findAll() throws Exception {
        assertEquals("MATCH p=()-->() RETURN p", new CypherQuery().findAll());
    }

    @Test
    public void delete() throws Exception {
        assertEquals("MATCH (n) WHERE id(n) = 123 OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().delete(123L));
    }

    @Test
    public void deleteAll() throws Exception {
        List<Long> ids = Arrays.asList(new Long[] { 123L, 234L, 345L });
        assertEquals("MATCH (n) WHERE id(n) in [123,234,345] OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().deleteAll(ids));
    }

    @Test
    public void deleteAllByLabel() throws Exception {
        assertEquals("MATCH (n:NODE) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().deleteByLabel("NODE"));
    }

    @Test
    public void purge() throws Exception {
        assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n", new CypherQuery().purge());
    }

    @Test
    public void testUpdateProperties() throws Exception {
        List<Property<String, Object>> properties = new ArrayList<>();
        properties.add(new Property<String, Object>("iProp", 42));
        properties.add(new Property<String, Object>("fProp", 3.1415928));
        properties.add(new Property<String, Object>("sProp", "Pie and the meaning of life"));
        assertEquals("MATCH (n) WHERE id(n) = 123 SET n.iProp=42,n.fProp=3.1415928,n.sProp=\\\"Pie and the meaning of life\\\"", new CypherQuery().updateProperties(123L, properties));
    }
}
