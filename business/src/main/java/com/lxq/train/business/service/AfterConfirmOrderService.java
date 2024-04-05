package com.lxq.train.business.service;

import com.lxq.train.business.domain.ConfirmOrder;
import com.lxq.train.business.domain.DailyTrainSeat;
import com.lxq.train.business.domain.DailyTrainTicket;
import com.lxq.train.business.enums.ConfirmOrderStatusEnum;
import com.lxq.train.business.feign.MemberFeign;
import com.lxq.train.business.mapper.ConfirmOrderMapper;
import com.lxq.train.business.mapper.DailyTrainSeatMapper;
import com.lxq.train.business.mapper.customer.DailyTrainTicketCustomerMapper;
import com.lxq.train.business.req.ConfirmOrderTicketReq;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.req.MemberTicketReq;
import com.lxq.train.common.resp.CommonResp;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class AfterConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Resource
    private DailyTrainTicketCustomerMapper dailyTrainTicketCustomerMapper;
    @Resource
    private MemberFeign memberFeign;
    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    /***
     * 选中座位后事务处理
     *  座位表修改售卖情况：sell
     *  余票详情修改余票
     *  为用户增加购票记录
     *  更新确认订单表状态为成功
     */
//    @Transactional
    @GlobalTransactional
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> finalSeatList, List<ConfirmOrderTicketReq>tickets, ConfirmOrder order) throws Exception {
        LOG.info("seata全局事务ID:{}", RootContext.getXID());
        for (int j = 0; j < finalSeatList.size(); j++) {
            DailyTrainSeat soldSeat = finalSeatList.get(j);
            DailyTrainSeat seatForUpdate = new DailyTrainSeat();
            seatForUpdate.setId(soldSeat.getId());
            seatForUpdate.setSell(soldSeat.getSell());
            seatForUpdate.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatForUpdate);
            LOG.info("最后的选座数据为：{}", soldSeat);
            LOG.info("更新的车座数据为：{}", seatForUpdate);

            // 计算这个票卖出去之后，影响了哪些站的余票库存
            // 影响的库存：本次选座之前没买过票的，和本次购买的区间有交集的区间
            // 假设有10个站，本次购买4～7站
            // 原售：001000001
            // 购买：000111000
            // 新售：001111001
            // 影响：XXX11111X
//            Integer startIndex = 4;
//            Integer endIndex = 7;
//            Integer minStartIndex = startIndex - 1;// 这里的1应该是查询到往前碰到的最后的一个0
//            Integer maxStartIndex = endIndex - 1;
//            Integer minEndIndex = startIndex + 1;
//            Integer maxEndIndex = endIndex + 1;// 1指的是往后碰到的最后一个0

            Integer startIndex = dailyTrainTicket.getStartIndex();
            Integer endIndex = dailyTrainTicket.getEndIndex();
            char[] sellChar = seatForUpdate.getSell().toCharArray();
            Integer maxStartIndex = endIndex - 1;
            Integer minEndIndex = startIndex + 1;
            Integer minStartIndex = 1;
            for (int i = startIndex - 2; i >= 0; i--)
                if (sellChar[i] == '1') {
                    minStartIndex = i + 2;
                    break;
                }
            LOG.info("影响的起始站的区间为：{}~{}", minStartIndex, maxStartIndex);
            Integer maxEndIndex = seatForUpdate.getSell().length() + 1;
            for (int i = endIndex - 1; i < seatForUpdate.getSell().length(); i++)
                if (sellChar[i] == '1') {
                    maxEndIndex = i + 1;
                    break;
                }
            LOG.info("影响的终点站的区间为：{}~{}", minEndIndex, maxEndIndex);
            dailyTrainTicketCustomerMapper.updateCountBySell(soldSeat.getDate(),
                    soldSeat.getTrainCode(), soldSeat.getSeatType(),
                    minStartIndex, maxStartIndex,
                    minEndIndex, maxEndIndex);

            MemberTicketReq req = new MemberTicketReq();
            req.setMemberId(LoginMemberContext.getId());
            req.setPassengerId(tickets.get(j).getPassengerId());
            req.setPassengerName(tickets.get(j).getPassengerName());
            req.setTrainDate(dailyTrainTicket.getDate());
            req.setTrainCode(dailyTrainTicket.getTrainCode());
            req.setCarriageIndex(soldSeat.getCarriageIndex());
            req.setSeatRow(soldSeat.getRow());
            req.setSeatCol(soldSeat.getCol());
            req.setStartStation(dailyTrainTicket.getStart());
            req.setStartTime(dailyTrainTicket.getStartTime());
            req.setEndStation(dailyTrainTicket.getEnd());
            req.setEndTime(dailyTrainTicket.getEndTime());
            req.setSeatType(soldSeat.getSeatType());
            CommonResp<Object> resp = memberFeign.save(req);
            LOG.info("调用member接口，返回:{}", resp);

            ConfirmOrder updateOrderStatus = new ConfirmOrder();
            updateOrderStatus.setId(order.getId());
            updateOrderStatus.setUpdateTime(new Date());
            updateOrderStatus.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrderMapper.updateByPrimaryKeySelective(updateOrderStatus);

            if (1 == 1) {
                throw new Exception("测试异常");
            }
        }
    }
}
