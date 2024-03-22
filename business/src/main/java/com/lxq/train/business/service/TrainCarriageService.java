package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.TrainCarriage;
import com.lxq.train.business.domain.TrainCarriageExample;
import com.lxq.train.business.enums.SeatColEnum;
import com.lxq.train.business.mapper.TrainCarriageMapper;
import com.lxq.train.business.req.TrainCarriageQueryReq;
import com.lxq.train.business.req.TrainCarriageSaveReq;
import com.lxq.train.business.resp.TrainCarriageQueryResp;
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
public class TrainCarriageService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;
    public void save(TrainCarriageSaveReq req){
        DateTime now = new DateTime();
        // 自动计算出列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        if (ObjectUtil.isNull(trainCarriage.getId())) {

            TrainCarriage trainCarriageDB = selectByUnique(req.getTrainCode(), req.getIndex());
            if (ObjectUtil.isNotEmpty(trainCarriageDB))
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);

            trainCarriage.setId(SnowUtil.getSnowFlakeNextId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
        }else{
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.updateByPrimaryKey(trainCarriage);
        }
    }

    private TrainCarriage selectByUnique(String trainCode, Integer index) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode).andIndexEqualTo(index);
        List<TrainCarriage> list = trainCarriageMapper.selectByExample(trainCarriageExample);
        if(CollUtil.isNotEmpty(list))
            return list.get(0);
        else
            return null;
    }

    public PageResp<TrainCarriageQueryResp> query(TrainCarriageQueryReq req){
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("train_code asc, `index` asc");
        TrainCarriageExample.Criteria trainCarriageExampleCriteria = trainCarriageExample.createCriteria();
        if(ObjectUtil.isNotEmpty(req.getTrainCode())){
            trainCarriageExampleCriteria.andTrainCodeEqualTo(req.getTrainCode());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<TrainCarriage> trainCarriageList = trainCarriageMapper.selectByExample(trainCarriageExample);

        PageInfo pageInfo = new PageInfo<>(trainCarriageList);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<TrainCarriageQueryResp> trainCarriageQueryResp = BeanUtil.copyToList(trainCarriageList, TrainCarriageQueryResp.class);

        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainCarriageQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        trainCarriageMapper.deleteByPrimaryKey(id);
    }

    public List<TrainCarriage> selectByTrainCode(String trainCode){
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.setOrderByClause("`index` asc");
        trainCarriageExample.createCriteria().andTrainCodeEqualTo(trainCode);
        return trainCarriageMapper.selectByExample(trainCarriageExample);
    }
}
