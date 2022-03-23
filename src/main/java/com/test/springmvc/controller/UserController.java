package com.test.springmvc.controller;

import com.test.springmvc.entity.User;
import com.tiny.springmvc.web.bind.annotation.RequestMapping;
import com.tiny.springmvc.web.bind.annotation.RequestParam;
import com.tiny.springmvc.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping(value="/login")
    public String login(
            @RequestParam("loginname")String loginname,
            @RequestParam("password")String password,
            Model model){

        System.out.println(loginname+"|"+password);
        Map map =new HashMap();
        map.put("user",new User(loginname,password));
        model.addAllAttributes(map);
        return "login";
    }


    @RequestMapping("/test")
    public String test(){

        return "Hello world!";
    }

    @RequestMapping("/json")
    @ResponseBody
    public User json(String name){
        System.out.println("=================:"+name);
        return new User("11","liaotao");
    }


}
