package org.neo4j.spring.integration.cineasts1.repo;

import org.neo4j.spring.integration.cineasts1.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {
}
