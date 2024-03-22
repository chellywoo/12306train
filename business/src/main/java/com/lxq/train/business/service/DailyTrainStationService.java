package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.DailyTrainStation;
import com.lxq.train.business.domain.DailyTrainStationExample;
import com.lxq.train.business.domain.TrainStation;
import com.lxq.train.business.mapper.DailyTrainStationMapper;
import com.lxq.train.business.req.DailyTrainStationQueryReq;
import com.lxq.train.business.req.DailyTrainStationSaveReq;
import com.lxq.train.business.resp.DailyTrainStationQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class DailyTrainStationService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);
    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;
    @Resource
    private TrainStationService trainStationService;

    public void save(DailyTrainStationSaveReq req){
        DateTime now = new DateTime();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }else{
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
        }
    }

    public PageResp<DailyTrainStationQueryResp> query(DailyTrainStationQueryReq req){
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.setOrderByClause("date desc, train_code asc, `index` asc");
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getDate())){
            criteria.andDateEqualTo(req.getDate());
        }
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);

        PageInfo pageInfo = new PageInfo<>(dailyTrainStationList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<DailyTrainStationQueryResp> dailyTrainStationQueryResp = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryResp.class);

        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainStationQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        dailyTrainStationMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(Date date, String trainCode){
        LOG.info("开始生成【{}】日车次【{}】车站数据", DateUtil.formatDate(date),trainCode);

        //删除某日某车次的车站信息
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);

        //查询出车次的车站信息
        List<TrainStation> trainStationList = trainStationService.selectByTrainCode(trainCode);

        if(CollUtil.isEmpty(trainStationList)){
            LOG.info("该车次没有车站基础数据，生成该车次的车站数据结束");
            return;
        }

        //增加数据到每日车站表中
        for (TrainStation trainStation : trainStationList) {
            DailyTrainStation  dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);

            DateTime now = new DateTime();
            dailyTrainStation.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            dailyTrainStationMapper.insert(dailyTrainStation);
        }
        LOG.info("生成【{}】日车次【{}】车站数据结束", DateUtil.formatDate(date),trainCode);
    }
}
