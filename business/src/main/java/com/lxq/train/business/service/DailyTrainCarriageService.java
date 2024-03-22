package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.*;
import com.lxq.train.business.enums.SeatColEnum;
import com.lxq.train.business.mapper.DailyTrainCarriageMapper;
import com.lxq.train.business.req.DailyTrainCarriageQueryReq;
import com.lxq.train.business.req.DailyTrainCarriageSaveReq;
import com.lxq.train.business.resp.DailyTrainCarriageQueryResp;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class DailyTrainCarriageService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);

    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;
    @Resource
    private TrainCarriageService trainCarriageService;
    public void save(DailyTrainCarriageSaveReq req){
        DateTime now = new DateTime();
        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(dailyTrainCarriage.getId())) {
            DailyTrainCarriage dailyTrainCarriageDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(dailyTrainCarriageDB))
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);

            dailyTrainCarriage.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }else{
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateByPrimaryKey(dailyTrainCarriage);
        }
    }

    private DailyTrainCarriage selectByUnique(String trainCode, Integer index) {
        DailyTrainCarriageExample example = new DailyTrainCarriageExample();
        example.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<DailyTrainCarriage> list = dailyTrainCarriageMapper.selectByExample(example);
        if(CollUtil.isNotEmpty(list))
            return list.get(0);
        else
            return null;
    }

    public PageResp<DailyTrainCarriageQueryResp> query(DailyTrainCarriageQueryReq req){
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.setOrderByClause("date desc, train_code asc, `index` asc");
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }

        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<DailyTrainCarriage> dailyTrainCarriageList = dailyTrainCarriageMapper.selectByExample(dailyTrainCarriageExample);

        PageInfo pageInfo = new PageInfo<>(dailyTrainCarriageList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<DailyTrainCarriageQueryResp> dailyTrainCarriageQueryResp = BeanUtil.copyToList(dailyTrainCarriageList, DailyTrainCarriageQueryResp.class);

        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainCarriageQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        dailyTrainCarriageMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(Date date, String trainCode){
        LOG.info("开始生成【{}】日车次【{}】车厢数据", DateUtil.formatDate(date),trainCode);

        //删除某日某车次的车厢信息
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainCarriageMapper.deleteByExample(dailyTrainCarriageExample);

        // 获取该车次的车厢数据
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);

        if(CollUtil.isEmpty(carriageList)){
            LOG.info("该车次没有车站基础数据，生成该车次的车厢数据结束");
            return;
        }

        //增加数据到每日车站表中
        for (TrainCarriage trainCarriage : carriageList) {
            DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);

            DateTime now = new DateTime();
            dailyTrainCarriage.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        }
        LOG.info("生成【{}】日车次【{}】车厢数据结束", DateUtil.formatDate(date),trainCode);


    }
}
