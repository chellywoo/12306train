package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.*;
import com.lxq.train.business.enums.ConfirmOrderStatusEnum;
import com.lxq.train.business.enums.SeatColEnum;
import com.lxq.train.business.enums.SeatTypeEnum;
import com.lxq.train.business.mapper.ConfirmOrderMapper;
import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.business.req.ConfirmOrderQueryReq;
import com.lxq.train.business.req.ConfirmOrderTicketReq;
import com.lxq.train.business.resp.ConfirmOrderQueryResp;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class ConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Autowired
//    private RedissonClient redissonClient;
    public void save(ConfirmOrderAcceptReq req){
        DateTime now = new DateTime();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowFlakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        }else{
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> query(ConfirmOrderQueryReq req){
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id DESC");
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<ConfirmOrderQueryResp> confirmOrderQueryResp = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(confirmOrderQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    @SentinelResource("doConfirm")
    public void doConfirm(ConfirmOrderAcceptReq req) {
        String key = DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(key, key, 2, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(setIfAbsent)){
            LOG.info("恭喜抢到锁了，lockKey:{}", key);
        }else{
            // 只是没抢到锁，之后还要继续操作的
            LOG.info("很遗憾没抢到锁，lockKey:{}", key);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR);
        }
//        RLock rLock = null;
//        try {
//            // 使用redisson，自带看门狗
//            rLock = redissonClient.getLock(key);
//            /**
//             * 等待时间，超时返回false；
//             * 锁时长，n秒后自动释放锁
//             */
////        boolean tryLock = rLock.tryLock(0, 10, TimeUnit.SECONDS);  不带看门狗
//            boolean tryLock = rLock.tryLock(0, TimeUnit.SECONDS);   // 带看门狗
//            if(tryLock){
//                LOG.info("恭喜抢到锁了，lockKey:{}", key);
//                for (int i = 0; i < 30; i++) {
//                    Long expire = redisTemplate.opsForValue().getOperations().getExpire(key);
//                    LOG.info("锁过期时间还剩：{}", expire);
//                    Thread.sleep(1000);
//                }
//            }else {
//                // 只是没抢到锁，之后还要继续操作的
//                LOG.info("很遗憾没抢到锁，lockKey:{}", key);
//                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR);
//            }
            // 省略业务数据校验，如车次是否存在，余票是否存在，车次是否在有效期内，tickets条数>0，同乘客同车次是否已经购买过
            // 做这个的目的是为了防止有人直接调用后端接口

            // 保存数据确认订单表，状态初始化
        try {
            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();

            DateTime now = new DateTime();
            ConfirmOrder order = new ConfirmOrder();
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
            confirmOrderMapper.insert(order);

            // 查出余票记录，得到真实的余票信息
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
            LOG.info("查出余票记录：{}", dailyTrainTicket);

            // 预扣减余票数据，并判断余票是否充足
            PreReduceTickets(req.getTickets(), dailyTrainTicket);

            // 最终的选座结果
            List<DailyTrainSeat> finalSeatList = new ArrayList<>();

            // 计算相对第一个座位的偏移值
            // 比如选择的是C1，D2时，偏移值是[0,5];
            // 读取ticket.seat，若存在数据，说明选座了，若为空，则说明为未选座
            ConfirmOrderTicketReq ticket1 = tickets.get(0);
            if (StrUtil.isBlank(ticket1.getSeat())) {
                LOG.info("本次购票未选座");
                for (ConfirmOrderTicketReq ticket : tickets) {
                    getSeat(finalSeatList, date, trainCode, ticket.getSeatTypeCode(), null, null, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());
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

                getSeat(finalSeatList, date, trainCode, ticket1.getSeatTypeCode(), ticket1.getSeat().substring(0, 1), offsetList, dailyTrainTicket.getStartIndex(), dailyTrainTicket.getEndIndex());

            }

            // 最终选座
            LOG.info("最终选座:{}", finalSeatList);

            // 选中座位后事务处理
            try {
                afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, tickets, order);
            } catch (Exception e) {
                LOG.error("保存购票信息失败", e);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
            }
//        } catch (InterruptedException e) {
//            LOG.error("购票异常", e);
//        }finally {
//            LOG.info("购票流程结束，释放锁");
//            if(null != rLock && rLock.isHeldByCurrentThread()){
//                rLock.unlock();
//            }
//        }
        } finally {
            LOG.info("购票流程结束，释放锁");
            redisTemplate.delete(key);
        }
    }

    /**
     * 挑选座位，如果用户自己选择了座位，则一次性选完。如果无选座，一个一个选
     * @param date
     * @param trainCode
     * @param seatType
     * @param col
     * @param offsetList
     */
    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date, String trainCode, String seatType, String col, List<Integer> offsetList, Integer startIndex, Integer endIndex) {
        List<DailyTrainSeat> getSeatList = new ArrayList<>();

        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatType);
        LOG.info("共查出{}个符合条件的车厢", carriageList.size());

        // 一个车厢一个车厢的获取座位数据
        for (DailyTrainCarriage carriage : carriageList) {
            LOG.info("开始从第{}车厢开始选座", carriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date, trainCode, carriage.getIndex());
            LOG.info("第{}车厢的座位数：{}", carriage.getIndex(), seatList.size());

            // 开始挑选座位
            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();
                String column = dailyTrainSeat.getCol();

                // 判断当前座位未被选中过
                boolean alreadySell = false;
                for (DailyTrainSeat finalSeat : finalSeatList) {
                    if (finalSeat.getId().equals(dailyTrainSeat.getId())) {
                        alreadySell = true;
                        break;
                    }
                }
                if (alreadySell) {
                    LOG.info("座位{}已被选中过，不能重复选中，继续选择下一个座位", seatIndex);
                    continue;
                }

                // 判断col值，如果有值的话需要比对列号
                if (StrUtil.isBlank(col)) {
                    LOG.info("未选座");
                } else {
                    if (!column.equals(col)) {
                        LOG.info("座位{}列值不对，继续判断下一个座位，当前列值{}，目标列值：{}", seatIndex, column, col);
                        continue;
                    }
                }

                // 判断座位是否可售
                boolean isSell = isSell(dailyTrainSeat, startIndex, endIndex);
                if (isSell) {
                    LOG.info("选中座位");
                    getSeatList.add(dailyTrainSeat);
//                    return;
                } else {
                    LOG.info("未选中座位");
                    continue;
                }

                // 判断offset列表，如果有值的话需要根据第一个座位获取座位
                boolean isGetAllOffsetSeat = true;
                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值:{}，校验座位是否可以被选", offsetList);
                    // 从索引1开始，索引0就是当前选中的票
                    for (int j = 1; j < offsetList.size(); j++) {
                        Integer offset = offsetList.get(j);
                        int nextIndex = i + offset;
                        //选座时，一定要在一个车厢
                        if (nextIndex > seatList.size()) {
                            LOG.info("座位{}不可选，偏移后的索引超出了车厢的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }

                        DailyTrainSeat nextDailyTrainSeat = seatList.get(nextIndex);
                        boolean isSellNext = isSell(nextDailyTrainSeat, startIndex, endIndex);
                        if (isSellNext) {
                            LOG.info("座位{}被选中", nextDailyTrainSeat.getCarriageSeatIndex());
                            getSeatList.add(nextDailyTrainSeat);
                        } else {
                            LOG.info("座位{}不可选", nextDailyTrainSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat){
                    getSeatList = new ArrayList<>();
                    continue;
                }

                // 保存选好的座位
                finalSeatList.addAll(getSeatList);
                return;
            }
        }

    }

    /**
     * 判断座位在某区间内是否可售
     * 因为座位中的数据是00010等方式存储的，所以需要判断是否可售
     * 现在有五站，那么数据库中是存储四个数字，如果为1000，表示第一个站不可售，那么对应的第一个站到任意站都不可售
     *
     * 如果购票成功，那么对应的要修改sell的值，将从起始站到终点站的可售改为1
     * 比如目前是1000，用户买了第二站到第四站的票，那么就需要将数据修改为1110
     * 方法：位运算：生成本次购买的车次sell值为0110，与当前数据库中存储的数据进行或运算，得到1110
     * @param dailyTrainSeat
     */
    private boolean isSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex){
        String sell = dailyTrainSeat.getSell(); // 1000
        String curSell = sell.substring(startIndex - 1, endIndex - 1); // 00
        if (Integer.parseInt(curSell) > 0) {
            LOG.info("座位{}在本次车站区间{}~{}已售票，不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        } else {
            LOG.info("座位{}在本次车站区间{}~{}未售票，选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            curSell = curSell.replace('0', '1');// 11
            curSell = StrUtil.fillBefore(curSell, '0', endIndex - 1); // 最后一位代表补完数据之后为几位 // 011
            curSell = StrUtil.fillAfter(curSell, '0', sell.length()); // 0110
            // 这里存在一个问题，如果curSell的值为01110，原始的为00001，那么相加为01111，但是转为int之后，自动设别为15
            // 但是再次转换为二进制之后，他只会生成4位的1111，不会按原始的位数生成
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            String newSell = NumberUtil.getBinaryStr(newSellInt);
            newSell = StrUtil.fillBefore(newSell, '0', sell.length());
            LOG.info("座位{}被选中，原售票信息：{}，车站区间{}~{}即：{}，最终售票信息：{}", dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
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
