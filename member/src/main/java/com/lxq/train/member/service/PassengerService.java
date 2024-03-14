package com.lxq.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.domain.Passenger;
import com.lxq.train.member.domain.PassengerExample;
import com.lxq.train.member.mapper.PassengerMapper;
import com.lxq.train.member.req.PassengerQueryReq;
import com.lxq.train.member.req.PassengerSaveReq;
import com.lxq.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


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

    public List<PassengerQueryResp> query(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria passengerExampleCriteria = passengerExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())){
            passengerExampleCriteria.andMemberIdEqualTo(req.getMemberId());
        }
        List<Passenger> passengerList = passengerMapper.selectByExample(passengerExample);
        List<PassengerQueryResp> passengerQueryResps = BeanUtil.copyToList(passengerList, PassengerQueryResp.class);
        return passengerQueryResps;

    }
}
