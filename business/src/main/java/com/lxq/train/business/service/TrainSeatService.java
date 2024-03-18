package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.TrainCarriage;
import com.lxq.train.business.domain.TrainSeat;
import com.lxq.train.business.domain.TrainSeatExample;
import com.lxq.train.business.enums.SeatColEnum;
import com.lxq.train.business.mapper.TrainSeatMapper;
import com.lxq.train.business.req.TrainSeatQueryReq;
import com.lxq.train.business.req.TrainSeatSaveReq;
import com.lxq.train.business.resp.TrainSeatQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.lxq.train.business.enums.SeatColEnum.getColsByType;


@Service
public class TrainSeatService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainSeatService.class);

    @Resource
    private TrainSeatMapper trainSeatMapper;
    @Resource
    private TrainCarriageService trainCarriageService;
    public void save(TrainSeatSaveReq trainSeatSaveReq){
        DateTime now = new DateTime();
        TrainSeat trainSeat = BeanUtil.copyProperties(trainSeatSaveReq, TrainSeat.class);
        if (ObjectUtil.isNull(trainSeat.getId())) {
            trainSeat.setId(SnowUtil.getSnowFlakeNextId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        }else{
            trainSeat.setUpdateTime(now);
            trainSeatMapper.updateByPrimaryKey(trainSeat);
        }
    }

    public PageResp<TrainSeatQueryResp> query(TrainSeatQueryReq req){
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.setOrderByClause("train_code asc, carriage_index asc, carriage_seat_index asc");
        TrainSeatExample.Criteria trainSeatExampleCriteria = trainSeatExample.createCriteria();
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            trainSeatExampleCriteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<TrainSeat> trainSeatList = trainSeatMapper.selectByExample(trainSeatExample);

        PageInfo pageInfo = new PageInfo<>(trainSeatList);
        LOG.info("车座总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<TrainSeatQueryResp> trainSeatQueryResp = BeanUtil.copyToList(trainSeatList, TrainSeatQueryResp.class);

        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainSeatQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        trainSeatMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void generateSeat(String trainCode){
        DateTime now = new DateTime();
        // 需要清空当前车次下的所有座位记录
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        TrainSeatExample.Criteria trainSeatExampleCriteria = trainSeatExample.createCriteria();
        trainSeatExampleCriteria.andTrainCodeEqualTo(trainCode);
        trainSeatMapper.deleteByExample(trainSeatExample);
        // 查询当前车次下的所有车厢
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        LOG.info("当前车次下的车厢数:" + carriageList.size());
        // 循环生成每个车厢的座位
        for (TrainCarriage trainCarriage : carriageList) {
            // 得到车厢数据：行数、座位类型（得到列数）
            Integer rowCount = trainCarriage.getRowCount();
            String seaType = trainCarriage.getSeatType();
            int seatIndex = 1;
            // 根据车厢的座位类型，筛选出所有的列，比如知道是一等座，那么得到的就是{ACDF}
            List<SeatColEnum> colsByType = getColsByType(seaType);
            LOG.info("车厢一共" + rowCount + "行");
            LOG.info("车厢的座位类型:{}" + colsByType);
            // 循环行数
            for (int row = 1; row <= rowCount; row++) {
                // 循环列数
                for(SeatColEnum seatColEnum : colsByType){
                    // 构造座位数据，保存数据库
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowFlakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(row),'0',2));
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(seaType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeatMapper.insert(trainSeat);
                }
            }

        }

    }
}
