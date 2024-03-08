package com.lxq.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.member.domain.Member;
import com.lxq.train.member.domain.MemberExample;
import com.lxq.train.member.mapper.MemberMapper;
import com.lxq.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public Long register( MemberRegisterReq memberRegisterReq){
        String mobile = memberRegisterReq.getMobile();
        //判断是否重复
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(list)){
            throw new BusinessException(BusinessExceptionEnum.MOBILE_ALREADY_EXIST);
        }
//            return list.get(0).getId();
//            throw new RuntimeException("手机号已被注册！");
        Member member = new Member();
        member.setId(System.currentTimeMillis());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }
}
