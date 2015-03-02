/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.integration.cineasts.partial;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.domain.cineasts.partial.Movie;
import org.neo4j.ogm.integration.InMemoryServerTest;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/*
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities, both when writing and reading
 *
 * In this scenario, the Role relationship, which is a RelationshipEntity
 * linking Actors and Movies, is referenced only from the Actor entity.
 */
public class RelationshipEntityPartialMappingTest extends InMemoryServerTest {

    private static SessionFactory sessionFactory;

    @Before
    public void init() throws IOException {
        setUp();
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.partial");
        session = sessionFactory.openSession("http://localhost:" + neoPort);
    }

    @Test
    public void testCreateAndReloadActorRoleAndMovie() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        keanu.addRole("Neo", matrix);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, 0L);

        assertEquals(1, keanu2.roles().size());

    }

    @Test
    public void testCreateAndReloadActorMultipleRolesAndMovies() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        Movie speed = new Movie("Speed");

        keanu.addRole("Neo", matrix);
        keanu.addRole("Jack Traven", speed);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, 0L);

        assertEquals(2, keanu2.roles().size());

        keanu2.addRole("John Constantine", new Movie("Constantine"));
        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, 0L);
        assertEquals(3, keanu3.roles().size());

    }

    @Test
    public void testCreateAndDeleteActorMultipleRolesAndMovies() {

        Actor keanu = new Actor("Keanu Reeves");
        Movie matrix = new Movie("The Matrix");
        Movie hp = new Movie("Harry Potter");

        keanu.addRole("Neo", matrix);
        keanu.addRole("Dumbledore", hp);

        session.save(keanu);

        Actor keanu2 = session.load(Actor.class, 0L);

        assertEquals(2, keanu2.roles().size());

        keanu2.removeRole("Dumbledore");

        session.save(keanu2);

        Actor keanu3 = session.load(Actor.class, 0L);
        assertEquals(1, keanu3.roles().size());


    }

}
