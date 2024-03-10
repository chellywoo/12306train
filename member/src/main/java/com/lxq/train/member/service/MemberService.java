package com.lxq.train.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.config.MemberApplication;
import com.lxq.train.member.domain.Member;
import com.lxq.train.member.domain.MemberExample;
import com.lxq.train.member.mapper.MemberMapper;
import com.lxq.train.member.req.MemberRegisterReq;
import com.lxq.train.member.req.MemberSendCodeReq;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberApplication.class);
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
        member.setId(SnowUtil.getSnowFlakeNextId());
        member.setMobile(mobile);
        memberMapper.insert(member);
        return member.getId();
    }

    //发送验证码部分
    public void sendCode(MemberSendCodeReq req){
        String mobile = req.getMobile();
        //判断是否重复
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);

        // 如果表中没有，那么将数据存到该表中
        if(CollUtil.isEmpty(list)){
            LOG.info("手机号正在注册中");
            Member member = new Member();
            member.setId(SnowUtil.getSnowFlakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        }else{
            LOG.info("手机号已注册");
        }

        // 生成验证码
        String code = RandomUtil.randomString(4);
        LOG.info("验证码为:{}", code);

        // 将验证码信息保存到短信记录表中，包括手机号、验证码、有效期、是否已使用、业务类型、发送时间、使用时间
        LOG.info("保存验证码信息");

        // 对接短信通道
        LOG.info("对接短信通道");

    }
}
