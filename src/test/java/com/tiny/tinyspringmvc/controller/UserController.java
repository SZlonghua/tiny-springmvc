package com.tiny.tinyspringmvc.controller;

import com.tiny.tinyspringmvc.annotation.Autowired;
import com.tiny.tinyspringmvc.annotation.Controller;
import com.tiny.tinyspringmvc.annotation.RequestMapping;
import com.tiny.tinyspringmvc.annotation.RequestParam;
import com.tiny.tinyspringmvc.entity.User;
import com.tiny.tinyspringmvc.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/index")
    public String index(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam("name")String name) throws IOException {
        String res = userService.get(name);
        System.out.println(name+"=>"+res);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(res);
        return "index";
    }

    @RequestMapping("/list")
    public String list(HttpServletRequest request,HttpServletResponse response)
            throws IOException{
        List<User> users = userService.list();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(users.toString());
        return "list";
    }
}
