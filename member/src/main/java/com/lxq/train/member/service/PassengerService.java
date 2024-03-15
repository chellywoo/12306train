package com.lxq.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.config.MemberApplication;
import com.lxq.train.member.domain.Passenger;
import com.lxq.train.member.domain.PassengerExample;
import com.lxq.train.member.mapper.PassengerMapper;
import com.lxq.train.member.req.PassengerQueryReq;
import com.lxq.train.member.req.PassengerSaveReq;
import com.lxq.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class PassengerService {
    private static final Logger LOG = LoggerFactory.getLogger(MemberApplication.class);

    @Resource
    private PassengerMapper passengerMapper;
    public void save(PassengerSaveReq passengerSaveReq){
        Date now = new Date();
        Passenger passenger = BeanUtil.copyProperties(passengerSaveReq, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowFlakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

    public PageResp<PassengerQueryResp> query(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria passengerExampleCriteria = passengerExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())){
            passengerExampleCriteria.andMemberIdEqualTo(req.getMemberId());
        }
        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);

        PageInfo pageInfo = new PageInfo<>(passengerList);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<PassengerQueryResp> passengerQueryResp = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);

        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(passengerQueryResp);
        return pageResp;

    }
}
