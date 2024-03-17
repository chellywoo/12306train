package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.business.domain.Train;
import com.lxq.train.business.domain.TrainExample;
import com.lxq.train.business.mapper.TrainMapper;
import com.lxq.train.business.req.TrainQueryReq;
import com.lxq.train.business.req.TrainSaveReq;
import com.lxq.train.business.resp.TrainQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TrainService {
    private static final Logger LOG = LoggerFactory.getLogger(TrainService.class);

    @Resource
    private TrainMapper trainMapper;
    public void save(TrainSaveReq trainSaveReq){
        DateTime now = new DateTime();
        Train train = BeanUtil.copyProperties(trainSaveReq, Train.class);
        if (ObjectUtil.isNull(train.getId())) {
            train.setId(SnowUtil.getSnowFlakeNextId());
            train.setCreateTime(now);
            train.setUpdateTime(now);
            trainMapper.insert(train);
        }else{
            train.setUpdateTime(now);
            trainMapper.updateByPrimaryKey(train);
        }
    }

    public PageResp<TrainQueryResp> query(TrainQueryReq req){
        TrainExample trainExample = new TrainExample();
        trainExample.setOrderByClause("id DESC");
        TrainExample.Criteria trainExampleCriteria = trainExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<Train> trainList = trainMapper.selectByExample(trainExample);

        PageInfo pageInfo = new PageInfo<>(trainList);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<TrainQueryResp> trainQueryResp = BeanUtil.copyToList(trainList, TrainQueryResp.class);

        PageResp<TrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        trainMapper.deleteByPrimaryKey(id);
    }
}
