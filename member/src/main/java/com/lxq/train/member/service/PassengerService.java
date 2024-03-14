package com.lxq.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.domain.Passenger;
import com.lxq.train.member.mapper.PassengerMapper;
import com.lxq.train.member.req.PassengerSaveReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class PassengerService {
//    private static final Logger LOG = LoggerFactory.getLogger(MemberApplication.class);

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
}
