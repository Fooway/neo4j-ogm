package org.neo4j.spring.example.world;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.spring.InMemoryNeo4jServer;
import org.neo4j.spring.Neo4jServer;
import org.neo4j.spring.domain.World;
import org.neo4j.spring.repositories.GraphRepository;
import org.neo4j.spring.repositories.impl.GraphRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

@Configuration
@ComponentScan({"org.neo4j.spring.repositories.impl", "org.neo4j.spring.example.world"})
@PropertySource("classpath:application.properties")
class ApplicationContext {

    @Resource
    private Environment environment;

    @Bean
    SessionFactory getSessionFactory() {
        return new SessionFactory(environment.getRequiredProperty("domain"));
    }

    @Bean
    Session getSession() throws Exception {
        // do this in production.
        //return getSessionFactory().openSession(environment.getRequiredProperty("url"));

        return getSessionFactory().openSession(getNeo4jServer().url());
    }

    @Bean
    Neo4jServer getNeo4jServer() throws Exception {
        return new InMemoryNeo4jServer();
        //return new LocalhostServer();
    }

    @Bean
    GraphRepository<World> worldRepository() throws Exception {
        return new GraphRepositoryImpl<>(World.class, getSession());
    }

}