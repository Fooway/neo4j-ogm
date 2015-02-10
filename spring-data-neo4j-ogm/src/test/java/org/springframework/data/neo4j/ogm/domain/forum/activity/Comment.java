package org.springframework.data.neo4j.ogm.domain.forum.activity;

import org.springframework.data.neo4j.ogm.annotation.Property;

public class Comment extends Activity {

    private String comment;

    @Property(name="remark")
    public String getComment() {
        return comment;
    }

    @Property(name="remark")
    public void setComment(String comment) {
        this.comment = comment;
    }
}
