package org.springframework.data.neo4j.integration.repositories;

import static org.junit.Assert.assertEquals;
import static org.neo4j.ogm.testutil.GraphTestUtils.assertSameGraph;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.testutil.WrappingServerIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.integration.repositories.context.PersistenceContext;
import org.springframework.data.neo4j.integration.repositories.domain.Movie;
import org.springframework.data.neo4j.integration.repositories.repo.MovieRepository;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryDefinitionTest extends WrappingServerIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    protected int neoServerPort() {
        return 7879;
    }

    @Test
    public void shouldProxyAndAutoImplementRepositoryDefinitionAnnotatedRepo() {
        Movie movie = new Movie("PF");
        movieRepository.save(movie);

        assertSameGraph(getDatabase(), "CREATE (m:Movie {title:'PF'})");

        assertEquals(1, IterableUtils.count(movieRepository.findAll()));
    }

}
