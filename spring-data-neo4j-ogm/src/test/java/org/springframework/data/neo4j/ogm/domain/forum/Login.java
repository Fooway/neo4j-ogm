package org.springframework.data.neo4j.ogm.domain.forum;

import org.springframework.data.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class Login {

    private Long id;
    private String userName;
    private String password;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
