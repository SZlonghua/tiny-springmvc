package com.tiny.tinyspringmvc.service;

import com.tiny.tinyspringmvc.entity.User;

import java.util.List;

public interface UserService {

    String get(String name);

    List<User> list();
}
