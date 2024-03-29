package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.*;
import com.lxq.train.business.enums.ConfirmOrderStatusEnum;
import com.lxq.train.business.enums.SeatColEnum;
import com.lxq.train.business.enums.SeatTypeEnum;
import com.lxq.train.business.mapper.ConformOrderMapper;
import com.lxq.train.business.req.ConfirmOrderTicketReq;
import com.lxq.train.business.req.ConformOrderAcceptReq;
import com.lxq.train.business.req.ConformOrderQueryReq;
import com.lxq.train.business.resp.ConformOrderQueryResp;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class ConformOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(ConformOrderService.class);

    @Resource
    private ConformOrderMapper conformOrderMapper;
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    public void save(ConformOrderAcceptReq req){
        DateTime now = new DateTime();
        ConformOrder conformOrder = BeanUtil.copyProperties(req, ConformOrder.class);
        if (ObjectUtil.isNull(conformOrder.getId())) {
            conformOrder.setId(SnowUtil.getSnowFlakeNextId());
            conformOrder.setCreateTime(now);
            conformOrder.setUpdateTime(now);
            conformOrderMapper.insert(conformOrder);
        }else{
            conformOrder.setUpdateTime(now);
            conformOrderMapper.updateByPrimaryKey(conformOrder);
        }
    }

    public PageResp<ConformOrderQueryResp> query(ConformOrderQueryReq req){
        ConformOrderExample conformOrderExample = new ConformOrderExample();
        conformOrderExample.setOrderByClause("id DESC");
        ConformOrderExample.Criteria criteria = conformOrderExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<ConformOrder> conformOrderList = conformOrderMapper.selectByExample(conformOrderExample);

        PageInfo pageInfo = new PageInfo<>(conformOrderList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<ConformOrderQueryResp> conformOrderQueryResp = BeanUtil.copyToList(conformOrderList, ConformOrderQueryResp.class);

        PageResp<ConformOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(conformOrderQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        conformOrderMapper.deleteByPrimaryKey(id);
    }

    public void doConfirm(ConformOrderAcceptReq req){
        // 省略业务数据校验，如车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已经购买过
        // 做这个的目的是为了防止有人直接调用后端接口

        // 保存数据确认订单表，状态初始化
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        DateTime now = new DateTime();
        ConformOrder order = new ConformOrder();
        order.setId(SnowUtil.getSnowFlakeNextId());
        order.setMemberId(LoginMemberContext.getId());
        order.setDate(date);
        order.setTrainCode(trainCode);
        order.setStart(start);
        order.setEnd(end);
        order.setDailyTrainTicketId(req.getDailyTrainTicketId());
        order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setTickets(JSON.toJSONString(tickets));
        conformOrderMapper.insert(order);

        // 查出余票记录，得到真实的余票信息
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
        LOG.info("查出余票记录：{}", dailyTrainTicket);

        // 预扣减余票数据，并判断余票是否充足
        PreReduceTickets(req.getTickets(), dailyTrainTicket);

        // 计算相对第一个座位的偏移值
        // 比如选择的是C1，D2时，偏移值是[0,5];
        // 读取ticket.seat，若存在数据，说明选座了，若为空，则说明为未选座
        ConfirmOrderTicketReq ticket1 = tickets.get(0);
        if (StrUtil.isBlank(ticket1.getSeat())) {
            LOG.info("本次购票未选座");
            for (ConfirmOrderTicketReq ticket : tickets) {
                getSeat(date, trainCode, ticket.getSeatTypeCode(), null, null);
            }
            LOG.info("未选座开始购票");
        } else {
            LOG.info("本次购票存在选座");
            // 查出本次选座的座位类型有哪些列，用于计算所选座位与第一个座位的偏移值，与预减库存时的方法类似
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(ticket1.getSeatTypeCode());
            LOG.info("本次选座的座位类型中的列为：{}", colEnumList);

            // 组成和前端类似的两排座位列表，用于作参照的座位列表
            List<String> seatList = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                for (int j = 0; j < colEnumList.size(); j++) {
                    seatList.add(colEnumList.get(j).getCode() + i);
                }
            }
            LOG.info("用于作参照的座位列表：{}", seatList);

            // 查询当前购票列表中的偏移值，[A1,c1,d1,f1,a2,c2,d2,f2],计算出的[c1,d2]的偏移量为[1,6]，但需要将其改为[0,5]
//            List<Integer> absoluteOffsetList = new ArrayList<>();
//            for (ConfirmOrderTicketReq ticketReq : tickets) {
//                int index = seatList.indexOf(ticketReq.getSeat());
//                absoluteOffsetList.add(index);
//            }
//            LOG.info("计算得到所有座位的绝对偏移值：{}", absoluteOffsetList);
//            上述的这个绝对偏移值没必要写，如果根据这个在遍历一次浪费时间，直接获取第一个座位的偏移量，得到后续的相对偏移量即可

            List<Integer> offsetList = new ArrayList<>();
            int offset = seatList.indexOf(ticket1.getSeat());
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                int index = seatList.indexOf(ticketReq.getSeat());
                offsetList.add(index - offset);
            }
            LOG.info("计算得到所有座位的相对偏移值：{}", offsetList);

            getSeat(date, trainCode, ticket1.getSeatTypeCode(), ticket1.getSeat().substring(0, 1), offsetList);

        }

        // 选座
            // 一个车厢一个车厢的获取座位数据

            // 挑选符合条件的座位，如果这个车厢不满足，则进入下个车厢，不允许一个订单中存在不同车厢的座位

        // 选中座位后事务处理
            // 座位表修改售卖情况：sell
            // 余票详情修改余票
            // 为用户增加购票记录
            // 更新确认订单表状态为成功
    }

    private void getSeat(Date date, String trainCode, String seatType, String col, List<Integer> offsetList){
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢", carriageList.size());

        // 一个车厢一个车厢的获取座位数据
        for (DailyTrainCarriage carriage : carriageList) {
            LOG.info("开始从第{}车厢开始选座", carriage.getIndex());
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date, trainCode, carriage.getIndex());
            LOG.info("第{}车厢的座位数：{}", carriage.getIndex(), seatList.size());

        }

    }

    private static void PreReduceTickets(List<ConfirmOrderTicketReq> tickets, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticket : tickets) {
            // 我自己写的方法，每次都去找对应的枚举值，有点麻烦，这样写很耗时间
//            if (Objects.equals(ticket.getSeatTypeCode(), SeatTypeEnum.YDZ.getCode())) {
//                ydz--;
//            }else if(Objects.equals(ticket.getSeatTypeCode(), SeatTypeEnum.EDZ.getCode()))
//                edz--;
            String seatTypeCode = ticket.getSeatTypeCode();
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, seatTypeCode);
//            LOG.info("车票类型：{}", seatTypeCode);
//            LOG.info("枚举：{}", seatTypeEnum);

            switch (seatTypeEnum) {
                case YDZ -> {
                    int countLast = dailyTrainTicket.getYdz() - 1;
//                    LOG.info("一等座余量:{}", dailyTrainTicket.getYdz());
                    if (countLast < 0)
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_YDZ_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setYdz(countLast);
                }
                case EDZ -> {
                    int countLast = dailyTrainTicket.getEdz() - 1;
                    if (countLast < 0)
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EDZ_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setEdz(countLast);
                }
                case RW -> {
                    int countLast = dailyTrainTicket.getRw() - 1;
                    if (countLast < 0)
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_RW_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setRw(countLast);
                }
                case YW -> {
                    int countLast = dailyTrainTicket.getYw() - 1;
                    if (countLast < 0)
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_YW_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setYw(countLast);
                }
            }
        }
    }
}
