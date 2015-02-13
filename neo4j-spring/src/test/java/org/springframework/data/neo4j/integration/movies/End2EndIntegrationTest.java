package org.springframework.data.neo4j.integration.movies;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.integration.movies.context.PersistenceContext;
import org.springframework.data.neo4j.integration.movies.domain.Cinema;
import org.springframework.data.neo4j.integration.movies.domain.Genre;
import org.springframework.data.neo4j.integration.movies.domain.Movie;
import org.springframework.data.neo4j.integration.movies.domain.ReleasedMovie;
import org.springframework.data.neo4j.integration.movies.domain.TempMovie;
import org.springframework.data.neo4j.integration.movies.domain.User;
import org.springframework.data.neo4j.integration.movies.repo.*;
import org.springframework.data.neo4j.integration.movies.service.UserService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.WrappingServerIntegrationTest;
import org.neo4j.tooling.GlobalGraphOperations;

import static org.junit.Assert.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.assertSameGraph;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class End2EndIntegrationTest extends WrappingServerIntegrationTest
{

    private final Logger logger = LoggerFactory.getLogger( End2EndIntegrationTest.class );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private AbstractAnnotatedEntityRepository abstractAnnotatedEntityRepository;

    @Autowired
    private AbstractEntityRepository abstractEntityRepository;

    @Autowired
    private TempMovieRepository tempMovieRepository;

    @Override
    protected int neoServerPort()
    {
        return 7879;
    }

    @Test
    public void shouldSaveUser()
    {
        User user = new User( "Michal" );
        userRepository.save( user );

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Michal'})" );
        assertEquals( 0L, (long) user.getId() );
    }

    @Test
    public void shouldSaveUserWithoutName()
    {
        User user = new User();
        userRepository.save( user );

        assertSameGraph( getDatabase(), "CREATE (u:User)" );
        assertEquals( 0L, (long) user.getId() );
    }

    @Test
    public void shouldSaveReleasedMovie()
    {

        Calendar cinemaReleaseDate = createDate( 1994, Calendar.SEPTEMBER, 10, "GMT" );
        Calendar cannesReleaseDate = createDate( 1994, Calendar.MAY, 12, "GMT" );

        ReleasedMovie releasedMovie = new ReleasedMovie( "Pulp Fiction", cinemaReleaseDate.getTime(),
                cannesReleaseDate.getTime() );

        abstractAnnotatedEntityRepository.save( releasedMovie );

        assertSameGraph( getDatabase(),
                "CREATE (m:ReleasedMovie:AbstractAnnotatedEntity {cinemaRelease:'1994-09-10T00:00:00.000Z'," +
                        "cannesRelease:768700800000,title:'Pulp Fiction'})" );
    }

    @Test
    public void shouldSaveReleasedMovie2()
    {

        Calendar cannesReleaseDate = createDate( 1994, Calendar.MAY, 12, "GMT" );

        ReleasedMovie releasedMovie = new ReleasedMovie( "Pulp Fiction", null, cannesReleaseDate.getTime() );

        abstractAnnotatedEntityRepository.save( releasedMovie );

        assertSameGraph( getDatabase(),
                "CREATE (m:ReleasedMovie:AbstractAnnotatedEntity {cannesRelease:768700800000,title:'Pulp Fiction'})" );

    }

    @Test
    public void shouldSaveMovie()
    {
        Movie movie = new Movie( "Pulp Fiction" );
        movie.setTags( new String[]{"cool", "classic"} );
        movie.setImage( new byte[]{1, 2, 3} );

        abstractEntityRepository.save( movie );

        // byte arrays have to be transferred with a JSON-supported format. Base64 is the default.
        assertSameGraph( getDatabase(), "CREATE (m:Movie {title:'Pulp Fiction', tags:['cool','classic'], " +
                "image:'AQID'})" );
    }

    @Test
    public void shouldSaveUsers()
    {
        Set<User> set = new HashSet<>();
        set.add( new User( "Michal" ) );
        set.add( new User( "Adam" ) );
        set.add( new User( "Vince" ) );

        userRepository.save( set );

        assertSameGraph( getDatabase(), "CREATE (:User {name:'Michal'})," +
                "(:User {name:'Vince'})," +
                "(:User {name:'Adam'})" );

        assertEquals( 3, userRepository.count() );
    }

    @Test
    public void shouldSaveUsers2()
    {
        List<User> list = new LinkedList<>();
        list.add( new User( "Michal" ) );
        list.add( new User( "Adam" ) );
        list.add( new User( "Vince" ) );

        userRepository.save( list );

        assertSameGraph( getDatabase(), "CREATE (:User {name:'Michal'})," +
                "(:User {name:'Vince'})," +
                "(:User {name:'Adam'})" );

        assertEquals( 3, userRepository.count() );
    }

    @Test
    public void shouldUpdateUserUsingRepository()
    {
        User user = userRepository.save( new User( "Michal" ) );
        user.setName( "Adam" );
        userRepository.save( user );

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Adam'})" );
        assertEquals( 0L, (long) user.getId() );
    }

    @Test
    @Ignore  // FIXME
    // this test expects the session/tx to check for dirty objects, which it currently does not do
    // you must save objects explicitly.
    public void shouldUpdateUserUsingTransactionalService()
    {
        User user = new User( "Michal" );
        userRepository.save( user );

        userService.updateUser( user, "Adam" ); //notice userRepository.save(..) isn't called,
        // not even in the service impl!

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Adam'})" );
        assertEquals( 0L, (long) user.getId() );
    }

    @Test
    public void shouldFindUser()
    {
        User user = new User( "Michal" );
        userRepository.save( user );

        User loaded = userRepository.findOne( 0L );

        assertEquals( 0L, (long) loaded.getId() );
        assertEquals( "Michal", loaded.getName() );

        assertTrue( loaded.equals( user ) );
        assertTrue( loaded == user );
    }

    @Test
    public void shouldFindUserWithoutName()
    {
        User user = new User();
        userRepository.save( user );

        User loaded = userRepository.findOne( 0L );

        assertEquals( 0L, (long) loaded.getId() );
        assertNull( loaded.getName() );

        assertTrue( loaded.equals( user ) );
        assertTrue( loaded == user );
    }

    @Test
    public void shouldDeleteUser()
    {
        User user = new User( "Michal" );
        userRepository.save( user );
        userRepository.delete( user );

        assertFalse( userRepository.findAll().iterator().hasNext() );
        assertFalse( userRepository.findAll( 1 ).iterator().hasNext() );
        assertFalse( userRepository.exists( 0L ) );
        assertEquals( 0, userRepository.count() );
        assertNull( userRepository.findOne( 0L ) );
        assertNull( userRepository.findOne( 0L, 10 ) );

        try ( Transaction tx = getDatabase().beginTx() )
        {
            assertFalse( GlobalGraphOperations.at( getDatabase() ).getAllNodes().iterator().hasNext() );
            tx.success();
        }
    }

    @Test
    public void shouldCreateUsersInMultipleThreads() throws InterruptedException, Neo4jFailedToStartException
    {
        waitForNeo4jToStart( 5000l );

        ExecutorService executor = Executors.newFixedThreadPool( 10 );
        CountDownLatch latch = new CountDownLatch( 100 );

        for ( int i = 0; i < 100; i++ )
        {
            executor.submit( new UserSaver( latch, i ) );
        }

        latch.await(); // pause until the count reaches 0
        executor.shutdown();

        assertEquals( 100, userRepository.count() );
    }

    private class UserSaver implements Runnable
    {

        private final int userNumber;
        private final CountDownLatch latch;

        public UserSaver( CountDownLatch latch, int userNumber )
        {
            this.latch = latch;
            this.userNumber = userNumber;
        }

        @Override
        public void run()
        {
            try
            {
                logger.info( "Calling userRepository.save() for user #" + this.userNumber );
                userRepository.save( new User( "User" + this.userNumber ) );
                logger.info( "Saved user #" + this.userNumber );
            }
            finally
            {
                latch.countDown();
            }
        }

    }

    @Test
    public void shouldSaveUserAndNewGenre()
    {
        User user = new User( "Michal" );
        user.interestedIn( new Genre( "Drama" ) );

        userRepository.save( user );

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Michal'})-[:INTERESTED]->(g:Genre {name:'Drama'})" );
    }

    @Test
    public void shouldSaveUserAndNewGenres()
    {
        User user = new User( "Michal" );
        user.interestedIn( new Genre( "Drama" ) );
        user.interestedIn( new Genre( "Historical" ) );
        user.interestedIn( new Genre( "Thriller" ) );

        userRepository.save( user );

        assertSameGraph( getDatabase(), "CREATE " +
                "(u:User {name:'Michal'})," +
                "(g1:Genre {name:'Drama'})," +
                "(g2:Genre {name:'Historical'})," +
                "(g3:Genre {name:'Thriller'})," +
                "(u)-[:INTERESTED]->(g1)," +
                "(u)-[:INTERESTED]->(g2)," +
                "(u)-[:INTERESTED]->(g3)" );
    }

    @Test
    public void shouldSaveUserAndNewGenre2()
    {
        User user = new User( "Michal" );
        user.interestedIn( new Genre( "Drama" ) );

        userRepository.save( user, 1 );

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Michal'})-[:INTERESTED]->(g:Genre {name:'Drama'})" );
    }

    @Test
    public void shouldSaveUserAndExistingGenre()
    {
        User michal = new User( "Michal" );
        Genre drama = new Genre( "Drama" );
        michal.interestedIn( drama );

        userRepository.save( michal );

        User vince = new User( "Vince" );
        vince.interestedIn( drama );

        userRepository.save( vince );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(v:User {name:'Vince'})," +
                "(g:Genre {name:'Drama'})," +
                "(m)-[:INTERESTED]->(g)," +
                "(v)-[:INTERESTED]->(g)" );
    }

    @Test
    public void shouldSaveUserButNotGenre()
    {
        User user = new User( "Michal" );
        user.interestedIn( new Genre( "Drama" ) );

        userRepository.save( user, 0 );

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Michal'})" );
    }

    @Test
    public void shouldUpdateGenreWhenSavedThroughUser()
    {
        User michal = new User( "Michal" );
        Genre drama = new Genre( "Drama" );
        michal.interestedIn( drama );

        userRepository.save( michal );

        drama.setName( "New Drama" );

        userRepository.save( michal );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'New Drama'})," +
                "(m)-[:INTERESTED]->(g)" );
    }

    @Test
    public void shouldRemoveGenreFromUser()
    {
        User michal = new User( "Michal" );
        Genre drama = new Genre( "Drama" );
        michal.interestedIn( drama );

        userRepository.save( michal );

        michal.notInterestedIn( drama );

        userRepository.save( michal );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'Drama'})" );
    }

    @Test
    public void shouldRemoveGenreFromUserUsingService()
    {
        User michal = new User( "Michal" );
        Genre drama = new Genre( "Drama" );
        michal.interestedIn( drama );

        userRepository.save( michal );

        userService.notInterestedIn( michal.getId(), drama.getId() );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(g:Genre {name:'Drama'})" );
    }

    @Test
    public void shouldAddNewVisitorToCinema()
    {
        Cinema cinema = new Cinema( "Odeon" );
        cinema.addVisitor( new User( "Michal" ) );

        cinemaRepository.save( cinema );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(c:Cinema {name:'Odeon'})," +
                "(m)-[:VISITED]->(c)" );
    }

    @Test
    public void shouldAddExistingVisitorToCinema()
    {
        User michal = new User( "Michal" );
        userRepository.save( michal );

        Cinema cinema = new Cinema( "Odeon" );
        cinema.addVisitor( michal );

        cinemaRepository.save( cinema );

        assertSameGraph( getDatabase(), "CREATE " +
                "(m:User {name:'Michal'})," +
                "(c:Cinema {name:'Odeon'})," +
                "(m)-[:VISITED]->(c)" );
    }

    @Test
    public void shouldBefriendPeople()
    {
        User michal = new User( "Michal" );
        michal.befriend( new User( "Adam" ) );
        userRepository.save( michal );

        try
        {
            assertSameGraph( getDatabase(), "CREATE (m:User {name:'Michal'})-[:FRIEND_OF]->(a:User {name:'Adam'})" );
        }
        catch ( AssertionError error )
        {
            assertSameGraph( getDatabase(), "CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User {name:'Adam'})" );
        }
    }

    @Test
    public void shouldLoadFriends()
    {
        new ExecutionEngine( getDatabase() ).execute( "CREATE (m:User {name:'Michal'})-[:FRIEND_OF]->(a:User " +
                "{name:'Adam'})" );

        User michal = userRepository.findByProperty( "name", "Michal" ).iterator().next();
        assertEquals( 1, michal.getFriends().size() );

        User adam = michal.getFriends().iterator().next();
        assertEquals( "Adam", adam.getName() );
        assertEquals( 1, adam.getFriends().size() );

        assertTrue( michal == adam.getFriends().iterator().next() );
        assertTrue( michal.equals( adam.getFriends().iterator().next() ) );
    }

    @Test
    public void shouldLoadFriends2()
    {
        new ExecutionEngine( getDatabase() ).execute( "CREATE (m:User {name:'Michal'})<-[:FRIEND_OF]-(a:User " +
                "{name:'Adam'})" );

        User michal = userRepository.findByProperty( "name", "Michal" ).iterator().next();
        assertEquals( 1, michal.getFriends().size() );

        User adam = michal.getFriends().iterator().next();
        assertEquals( "Adam", adam.getName() );
        assertEquals( 1, adam.getFriends().size() );

        assertTrue( michal == adam.getFriends().iterator().next() );
        assertTrue( michal.equals( adam.getFriends().iterator().next() ) );
    }


    @Test
    public void shouldSaveNewUserAndNewMovieWithRatings()
    {
        User user = new User( "Michal" );
        TempMovie movie = new TempMovie( "Pulp Fiction" );
        user.rate( movie, 5, "Best movie ever" );
        userRepository.save( user );

        userRepository.findByProperty( "name", "Michal" ).iterator().next();

        assertSameGraph( getDatabase(), "CREATE (u:User {name:'Michal'})-[:RATED {stars:5, " +
                "comment:'Best movie ever'}]->(m:Movie {title:'Pulp Fiction'})" );
    }

    @Test
    public void shouldSaveNewUserRatingsForAnExistingMovie()
    {
        TempMovie movie = new TempMovie( "Pulp Fiction" );
        //Save the movie
        movie = tempMovieRepository.save(movie);

        //Create a new user and rate an existing movie
        User user = new User( "Michal" );
        user.rate( movie, 5, "Best movie ever" );
        userRepository.save( user );

        TempMovie tempMovie = tempMovieRepository.findByProperty("title", "Pulp Fiction").iterator().next();
        assertEquals(1,tempMovie.getRatings().size());
    }

    private Calendar createDate( int y, int m, int d, String tz )
    {

        Calendar calendar = Calendar.getInstance();

        calendar.set( y, m, d );
        calendar.setTimeZone( TimeZone.getTimeZone( tz ) );

        // need to do this to ensure the test passes, or the calendar will use the current time's values
        // an alternative (better) would be to specify an date format using one of the @Date converters
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.SECOND, 0 );
        calendar.set( Calendar.MILLISECOND, 0 );

        return calendar;
    }


    private void waitForNeo4jToStart( long maxTimeToWait ) throws Neo4jFailedToStartException
    {
        long startTime = System.currentTimeMillis();
        org.neo4j.ogm.session.transaction.Transaction transaction;

        do
        {
            transaction = new SessionFactory().openSession( baseNeoUrl() ).beginTransaction();
        } while ( transaction == null && System.currentTimeMillis() - startTime <= maxTimeToWait );

        if ( transaction == null )
        {
            throw new Neo4jFailedToStartException( maxTimeToWait );
        }
    }

    private static class Neo4jFailedToStartException extends Exception
    {
        private Neo4jFailedToStartException( long timeoutValue )
        {
            super( String.format( "Could not start neo4j instance in [%d] ms", timeoutValue ) );
        }
    }
}
