package com.lxq.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.config.MemberApplication;
import com.lxq.train.member.domain.${Domain};
import com.lxq.train.member.domain.${Domain}Example;
import com.lxq.train.member.mapper.${Domain}Mapper;
import com.lxq.train.member.req.${Domain}QueryReq;
import com.lxq.train.member.req.${Domain}SaveReq;
import com.lxq.train.member.resp.${Domain}QueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ${Domain}Service {
    private static final Logger LOG = LoggerFactory.getLogger(MemberApplication.class);

    @Resource
    private ${Domain}Mapper ${domain}Mapper;
    public void save(${Domain}SaveReq ${domain}SaveReq){
        DateTime now = new DateTime();
        ${Domain} ${domain} = BeanUtil.copyProperties(${domain}SaveReq, ${Domain}.class);
        if (ObjectUtil.isNull(${domain}.getId())) {
            ${domain}.setMemberId(LoginMemberContext.getId());
            ${domain}.setId(SnowUtil.getSnowFlakeNextId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else{
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.updateByPrimaryKey(${domain});
        }
    }

    public PageResp<${Domain}QueryResp> query(${Domain}QueryReq req){
        ${Domain}Example ${domain}Example = new ${Domain}Example();
        ${domain}Example.setOrderByClause("id DESC");
        ${Domain}Example.Criteria ${domain}ExampleCriteria = ${domain}Example.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())){
            ${domain}ExampleCriteria.andMemberIdEqualTo(req.getMemberId());
        }
        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<${Domain}> ${domain}List = ${domain}Mapper.selectByExample(${domain}Example);

        PageInfo pageInfo = new PageInfo<>(${domain}List);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<${Domain}QueryResp> ${domain}QueryResp = BeanUtil.copyToList(${domain}List, ${Domain}QueryResp.class);

        PageResp<${Domain}QueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(${domain}QueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        ${domain}Mapper.deleteByPrimaryKey(id);
    }
}
