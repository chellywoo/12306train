package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.TrainStation;
import com.lxq.train.business.domain.TrainStationExample;
import com.lxq.train.business.mapper.TrainStationMapper;
import com.lxq.train.business.req.TrainStationQueryReq;
import com.lxq.train.business.req.TrainStationSaveReq;
import com.lxq.train.business.resp.TrainStationQueryResp;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TrainStationService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;
    public void save(TrainStationSaveReq req){
        DateTime now = new DateTime();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);
        if (ObjectUtil.isNull(trainStation.getId())) {
            TrainStation trainStationDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(trainStationDB))
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            trainStationDB = selectByUnique(req.getTrainCode(), req.getName());
            if(ObjectUtil.isNotEmpty(trainStationDB))
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);

            trainStation.setId(SnowUtil.getSnowFlakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        }else{
            trainStation.setUpdateTime(now);
            trainStationMapper.updateByPrimaryKey(trainStation);
        }
    }

    private TrainStation selectByUnique(String trainCode, Integer index) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainStation> list = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(list))
            return list.get(0);
        else
            return null;
    }

    private TrainStation selectByUnique(String trainCode, String name) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode).andNameEqualTo(name);
        List<TrainStation> list = trainStationMapper.selectByExample(trainStationExample);
        if(CollUtil.isNotEmpty(list))
            return list.get(0);
        else
            return null;
    }

    public PageResp<TrainStationQueryResp> query(TrainStationQueryReq req){
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code asc, `index` asc");
        TrainStationExample.Criteria trainStationExampleCriteria = trainStationExample.createCriteria();
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            trainStationExampleCriteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<TrainStation> trainStationList = trainStationMapper.selectByExample(trainStationExample);

        PageInfo pageInfo = new PageInfo<>(trainStationList);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<TrainStationQueryResp> trainStationQueryResp = BeanUtil.copyToList(trainStationList, TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainStationQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        trainStationMapper.deleteByPrimaryKey(id);
    }

    public List<TrainStation> selectByTrainCode(String trainCode) {
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.setOrderByClause("train_code asc, `index` asc");
        trainStationExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(trainStationExample);
    }
}
