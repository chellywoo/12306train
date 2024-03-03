package com.lxq.train.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/hello1111")
    public String hello(){
        return "hello world1123123";
    }
}
