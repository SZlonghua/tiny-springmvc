package com.test.springmvc.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {

    private String id;
    private String name;

    public User() {
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
