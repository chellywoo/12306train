package com.lxq.train.member.controller;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.member.req.MemberRegisterReq;
import com.lxq.train.member.req.MemberSendCodeReq;
import com.lxq.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
    public CommonResp<Integer> count(){
        int count = memberService.count();
        return new CommonResp<Integer>(count);
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
    public CommonResp register(@Valid MemberRegisterReq req){
        Long l = memberService.register(req);
        return new CommonResp<>("注册成功",l);
    }

    @PostMapping("/sendCode")
    public CommonResp sendCode(@Valid MemberSendCodeReq req){
        memberService.sendCode(req);
        return new CommonResp<>();
    }
}
