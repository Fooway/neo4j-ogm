package org.neo4j.ogm.metadata;

import org.junit.Test;
import org.neo4j.ogm.entityaccess.MethodEntityAccess;
import org.neo4j.ogm.mapper.domain.social.Individual;
import org.neo4j.ogm.metadata.dictionary.MethodDictionary;
import org.neo4j.ogm.metadata.info.DomainInfo;
import org.neo4j.ogm.strategy.simple.SimpleMethodDictionary;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class SetterEntityAccessTest {

    private MethodDictionary socialDictionary = new SimpleMethodDictionary(new DomainInfo("org.neo4j.ogm.mapper.domain.social"));

    /**
     * The throwing of Exceptions here is by design. We expect that
     * the {@link PersistentField} objects will contain the definitive mappings
     * of property names to and from node & edge properties. In the event that
     * this mapping is wrong, we should throw an Exception rather than
     * consuming the error and continuing, because an incorrect map is
     * a bug.
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMappingToNullPropertyName() throws Exception {
        Individual peter = new Individual();
        MethodEntityAccess.forProperty(socialDictionary, null).set(peter, "Peter");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMappingToEmptyPropertyName() throws Exception {
        Individual peter = new Individual();
        MethodEntityAccess.forProperty(socialDictionary, "").set(peter, "Peter");
    }

    @Test(expected = MappingException.class)
    public void shouldThrowNSMEWhenMappingToNonExistentPropertyName() throws Exception {
        Individual peter = new Individual();
        MethodEntityAccess.forProperty(socialDictionary, "height").set(peter, 183);
    }

    @Test
    public void shouldWriteAllValidPropertyValues() throws Exception {

        Individual peter = new Individual();
        Individual paul = new Individual();
        Individual mary = new Individual();
        List<Individual> friends = new ArrayList<>();

        friends.add(paul);
        friends.add(mary);

        MethodEntityAccess.forProperty(socialDictionary,"name").set(peter, "Peter");
        MethodEntityAccess.forProperty(socialDictionary,"age").set(peter, 34);
        MethodEntityAccess.forProperty(socialDictionary,"friends").set(peter, friends);
        MethodEntityAccess.forProperty(socialDictionary,"primitiveIntArray").set(peter, new int[] { 0, 1, 2, 3, 4, 5 });

        assertEquals("Peter", peter.getName());
        assertEquals(34, peter.getAge());
        assertTrue(peter.getFriends().contains(mary));
        assertTrue(peter.getFriends().contains(paul));

        for (int i = 0; i < 6; i++) {
            assertEquals(i, peter.getPrimitiveIntArray()[i]);
        }
    }

}
