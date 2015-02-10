package org.springframework.data.neo4j.ogm.domain.forum;

import org.springframework.data.neo4j.ogm.annotation.GraphId;
import org.springframework.data.neo4j.ogm.annotation.Relationship;
import org.springframework.data.neo4j.ogm.domain.forum.activity.Post;

import java.util.List;

public class Topic {

    @Relationship(type ="HAS_POSTS")
    private List<Post> posts;
    private Boolean inActive;

    @GraphId
    private Long topicId;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        setInActive(posts.isEmpty());
        this.posts = posts;
    }

    public Boolean getInActive() {
        return inActive;
    }

    public void setInActive(Boolean inActive) {
        this.inActive = inActive;
    }
}
