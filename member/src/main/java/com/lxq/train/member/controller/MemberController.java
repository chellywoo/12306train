package com.lxq.train.member.controller;

import com.lxq.train.member.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {
    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public int count(){
        return memberService.count();
    }
    @GetMapping("/insert")
    public int insert(){
        return memberService.insert();
    }

    @GetMapping("/delete")
    public int delete(){
        return memberService.delete();
    }

    @PostMapping("/register")
    public Long register(String mobile){
        return memberService.register(mobile);
    }
}
