package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.DailyTrain;
import com.lxq.train.business.domain.DailyTrainExample;
import com.lxq.train.business.domain.Train;
import com.lxq.train.business.mapper.DailyTrainMapper;
import com.lxq.train.business.req.DailyTrainQueryReq;
import com.lxq.train.business.req.DailyTrainSaveReq;
import com.lxq.train.business.resp.DailyTrainQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class DailyTrainService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);
    @Resource
    private DailyTrainMapper dailyTrainMapper;
    @Resource
    private TrainService trainService;
    @Resource
    private DailyTrainStationService dailyTrainStationService;
    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    public void save(DailyTrainSaveReq req) {
        DateTime now = new DateTime();
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);
        if (ObjectUtil.isNull(dailyTrain.getId())) {
            dailyTrain.setId(SnowUtil.getSnowFlakeNextId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        } else {
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateByPrimaryKey(dailyTrain);
        }
    }

    public PageResp<DailyTrainQueryResp> query(DailyTrainQueryReq req) {
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.setOrderByClause("date desc, code asc");
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();

        if (ObjectUtil.isNotNull(req.getDate())) {
            criteria.andDateEqualTo(req.getDate());
        }

        if (ObjectUtil.isNotEmpty(req.getCode())) {
            criteria.andCodeEqualTo(req.getCode());
        }

        LOG.info("查询页数为：" + req.getPage());
        LOG.info("每页条数为：" + req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrain> dailyTrainList = dailyTrainMapper.selectByExample(dailyTrainExample);

        PageInfo pageInfo = new PageInfo<>(dailyTrainList);
        LOG.info("乘客总数为：" + pageInfo.getTotal());
        LOG.info("最大分配的页数为：" + pageInfo.getPages());
        List<DailyTrainQueryResp> dailyTrainQueryResp = BeanUtil.copyToList(dailyTrainList, DailyTrainQueryResp.class);

        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(dailyTrainQueryResp);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(Date date) {
        List<Train> trainList = trainService.selectAll();
        if (CollUtil.isEmpty(trainList)) {
            LOG.info("当前车次列表为空，任务结束");
            return;
        }
        for (Train train : trainList) {
            generateDailyTrain(date, train);
        }
    }

    private void generateDailyTrain(Date date, Train train) {
        LOG.info("开始生成【{}】日车次【{}】数据", DateUtil.formatDate(date), train.getCode());
        // 删除现有车次
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.createCriteria().andDateEqualTo(date).andCodeEqualTo(train.getCode());
        dailyTrainMapper.deleteByExample(dailyTrainExample);

        //增加车次数据
        DateTime now = new DateTime();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowFlakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);
        dailyTrainStationService.generateDaily(date, train.getCode());
        dailyTrainCarriageService.generateDaily(date, train.getCode());
        dailyTrainSeatService.generateDaily(date, train.getCode());
        dailyTrainTicketService.generateDaily(date,train.getCode());
        LOG.info("生成【{}】日车次【{}】数据结束", DateUtil.formatDate(date), train.getCode());
    }
    public void generateOnceTrain(Date date, String trainCode) {
        //增加车次数据
        Train train = trainService.selectByUnique(trainCode);
        generateDailyTrain(date, train);
    }
}
