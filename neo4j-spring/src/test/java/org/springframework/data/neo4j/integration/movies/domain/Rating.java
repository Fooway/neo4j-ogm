package org.springframework.data.neo4j.integration.movies.domain;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class Rating {
    private Long id;

    @StartNode
    private User user;
    @EndNode
    private TempMovie movie;
    private int stars;
    private String comment;

    public Rating(User user, TempMovie movie, int stars, String comment) {
        this.user = user;
        this.movie = movie;
        this.stars = stars;
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public TempMovie getMovie() {
        return movie;
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }
}
