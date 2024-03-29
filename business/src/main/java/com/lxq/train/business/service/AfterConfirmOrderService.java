package com.lxq.train.business.service;

import com.lxq.train.business.domain.DailyTrainSeat;
import com.lxq.train.business.mapper.DailyTrainSeatMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@Service
public class AfterConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    /***
     * 选中座位后事务处理
     *  座位表修改售卖情况：sell
     *  余票详情修改余票
     *  为用户增加购票记录
     *  更新确认订单表状态为成功
     * @param finalSeatList
     */
    @Transactional
    public void afterDoConfirm(List<DailyTrainSeat> finalSeatList){
        for (DailyTrainSeat selledSeat : finalSeatList) {
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(selledSeat.getId());
            seatForUpdate.setSell(selledSeat.getSell());
            seatForUpdate.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatForUpdate);
        }
    }
}
