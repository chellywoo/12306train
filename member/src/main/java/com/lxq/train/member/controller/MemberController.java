package com.lxq.train.member.controller;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.member.req.MemberLoginReq;
import com.lxq.train.member.req.MemberRegisterReq;
import com.lxq.train.member.req.MemberSendCodeReq;
import com.lxq.train.member.resp.MemberLoginResp;
import com.lxq.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public CommonResp sendCode(@Valid @RequestBody MemberSendCodeReq req){
        memberService.sendCode(req);
        return new CommonResp<>();
    }

    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq req){
        MemberLoginResp loginResp = memberService.login(req);
        return new CommonResp<>(loginResp);
    }
}
