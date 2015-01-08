package org.neo4j.spring.integration.cineasts1.repo;

import org.neo4j.spring.integration.cineasts1.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GraphRepository<User> {
}
