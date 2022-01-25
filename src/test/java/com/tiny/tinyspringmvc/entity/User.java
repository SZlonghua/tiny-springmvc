package com.tiny.tinyspringmvc.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String id;
    private String username;
    private String password;

    @Override
    public String toString() {
        return "{\"id\":" + id + ", \"username\":\"" + username + "\", \"password\":\""
                + password + "\"}";
    }
    public User() {}
    public User(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

}
