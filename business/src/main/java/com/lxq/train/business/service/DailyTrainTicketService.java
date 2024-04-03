package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.DailyTrain;
import com.lxq.train.business.domain.DailyTrainTicket;
import com.lxq.train.business.domain.DailyTrainTicketExample;
import com.lxq.train.business.domain.TrainStation;
import com.lxq.train.business.enums.SeatTypeEnum;
import com.lxq.train.business.enums.TrainTypeEnum;
import com.lxq.train.business.mapper.DailyTrainTicketMapper;
import com.lxq.train.business.req.DailyTrainTicketQueryReq;
import com.lxq.train.business.req.DailyTrainTicketSaveReq;
import com.lxq.train.business.resp.DailyTrainTicketQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;


@Service
public class DailyTrainTicketService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);
    @Resource
    private TrainStationService trainStationService;
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    public void save(DailyTrainTicketSaveReq req){
        DateTime now = new DateTime();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        }else{
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateByPrimaryKey(dailyTrainTicket);
        }
    }

    @Cacheable(value = "DailyTrainTicketService.query3")
    public PageResp<DailyTrainTicketQueryResp> query3(DailyTrainTicketQueryReq req) {
        LOG.info("测试缓存击穿");
        return null;
    }
    @CachePut(value = "DailyTrainTicketService.query")
    public PageResp<DailyTrainTicketQueryResp> query2(DailyTrainTicketQueryReq req) {
        return query(req);
    }
    @Cacheable(value="DailyTrainTicketService.query")
    public PageResp<DailyTrainTicketQueryResp> query(DailyTrainTicketQueryReq req){
        /**
         * 常见的缓存过期策略
         *  TTL：超时时间
         *  LRU：最近最久未使用，删除长时间未使用的数据
         *  LFU：最近最不经常使用，删除使用次数最低的数据
         *  FIFO：先进先出
         *  Random：随机淘汰
         */
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.setOrderByClause("date desc, train_code asc");
        DailyTrainTicketExample.Criteria criteria = dailyTrainTicketExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getDate()))
            criteria.andDateEqualTo(req.getDate());

        if(ObjectUtil.isNotEmpty(req.getTrainCode()))
            criteria.andTrainCodeEqualTo(req.getTrainCode());

        if(ObjectUtil.isNotEmpty(req.getStart()))
            criteria.andStartEqualTo(req.getStart());

        if(ObjectUtil.isNotEmpty(req.getEnd()))
            criteria.andEndEqualTo(req.getEnd());

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<DailyTrainTicket> dailyTrainTicketList = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);

        PageInfo pageInfo = new PageInfo<>(dailyTrainTicketList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<DailyTrainTicketQueryResp> dailyTrainTicketQueryResp = BeanUtil.copyToList(dailyTrainTicketList, DailyTrainTicketQueryResp.class);

        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainTicketQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        dailyTrainTicketMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(DailyTrain train, Date date, String trainCode){
        LOG.info("开始生成【{}】日车次【{}】余票数据", DateUtil.formatDate(date),trainCode);

        //删除某日某车次的余票信息
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainTicketMapper.deleteByExample(dailyTrainTicketExample);

        //查询出车次的余票信息
        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);

        if(CollUtil.isEmpty(stationList)){
            LOG.info("该车次没有车站基础数据，生成该车次的余票数据结束");
            return;
        }

        // 计算每个座位类型的座位数量
        int ydz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YDZ.getCode());
        int edz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.EDZ.getCode());
        int rw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.RW.getCode());
        int yw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YW.getCode());

        // 计算每个座位的价位 票价 = 车类型 * 累计里程数 * 座位类型
        // 先要知道车的类型
        String type = train.getType();
        // 计算里程数
        BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, type);

        //增加数据到每日余票表中
        for (int i = 0; i < stationList.size(); i++) {
            DateTime now = new DateTime();
            TrainStation trainStationStart = stationList.get(i);
            BigDecimal sumKM = BigDecimal.ZERO;
            for (int j = i + 1; j < stationList.size(); j++) {
                TrainStation trainStationEnd = stationList.get(j);
                sumKM = sumKM.add(trainStationEnd.getKm());
                BigDecimal ydzPrice = sumKM.multiply(priceRate).multiply(SeatTypeEnum.YDZ.getPrice()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal edzPrice = sumKM.multiply(priceRate).multiply(SeatTypeEnum.EDZ.getPrice()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rwPrice = sumKM.multiply(priceRate).multiply(SeatTypeEnum.RW.getPrice()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal ywPrice = sumKM.multiply(priceRate).multiply(SeatTypeEnum.YW.getPrice()).setScale(2, RoundingMode.HALF_UP);

                DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                dailyTrainTicket.setId(SnowUtil.getSnowFlakeNextId());
                dailyTrainTicket.setDate(date);
                dailyTrainTicket.setTrainCode(trainCode);
                dailyTrainTicket.setStart(trainStationStart.getName());
                dailyTrainTicket.setStartPinyin(trainStationStart.getNamePinyin());
                dailyTrainTicket.setStartTime(trainStationStart.getOutTime());
                dailyTrainTicket.setStartIndex(trainStationStart.getIndex());
                dailyTrainTicket.setEnd(trainStationEnd.getName());
                dailyTrainTicket.setEndPinyin(trainStationEnd.getNamePinyin());
                dailyTrainTicket.setEndTime(trainStationEnd.getInTime());
                dailyTrainTicket.setEndIndex(trainStationEnd.getIndex());
                dailyTrainTicket.setYdz(ydz);
                dailyTrainTicket.setYdzPrice(ydzPrice);
                dailyTrainTicket.setEdz(edz);
                dailyTrainTicket.setEdzPrice(edzPrice);
                dailyTrainTicket.setRw(rw);
                dailyTrainTicket.setRwPrice(rwPrice);
                dailyTrainTicket.setYw(yw);
                dailyTrainTicket.setYwPrice(ywPrice);
                dailyTrainTicket.setCreateTime(now);
                dailyTrainTicket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(dailyTrainTicket);
            }
        }
        LOG.info("生成【{}】日车次【{}】余票数据结束", DateUtil.formatDate(date),trainCode);
    }

    public DailyTrainTicket selectByUnique(Date date, String trainCode, String start, String end) {
        DailyTrainTicketExample dailyTrainTicketExample = new DailyTrainTicketExample();
        dailyTrainTicketExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode).andStartEqualTo(start).andEndEqualTo(end);
        List<DailyTrainTicket> list = dailyTrainTicketMapper.selectByExample(dailyTrainTicketExample);
        if(CollUtil.isNotEmpty(list))
            return list.get(0);
        else
            return null;
    }
}
