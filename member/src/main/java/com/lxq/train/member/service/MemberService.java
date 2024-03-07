package com.lxq.train.member.service;

import com.lxq.train.member.domain.Member;
import com.lxq.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public int count(){
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    public int insert(){
        Member member = new Member();
        member.setId(2L);
        member.setMobile("13720239041");
        return memberMapper.insert(member);
    }

    public int delete(){
        return memberMapper.deleteByPrimaryKey(2L);
    }
}
