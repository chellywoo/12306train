package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.business.domain.Station;
import com.lxq.train.business.domain.StationExample;
import com.lxq.train.business.mapper.StationMapper;
import com.lxq.train.business.req.StationQueryReq;
import com.lxq.train.business.req.StationSaveReq;
import com.lxq.train.business.resp.StationQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StationService {
    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;
    public void save(StationSaveReq stationSaveReq){
        DateTime now = new DateTime();
        Station station = BeanUtil.copyProperties(stationSaveReq, Station.class);
        if (ObjectUtil.isNull(station.getId())) {
            station.setId(SnowUtil.getSnowFlakeNextId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        }else{
            station.setUpdateTime(now);
            stationMapper.updateByPrimaryKey(station);
        }
    }

    public PageResp<StationQueryResp> query(StationQueryReq req){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("id DESC");
        StationExample.Criteria stationExampleCriteria = stationExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<Station> stationList = stationMapper.selectByExample(stationExample);

        PageInfo pageInfo = new PageInfo<>(stationList);
        LOG.info("乘客总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<StationQueryResp> stationQueryResp = BeanUtil.copyToList(stationList, StationQueryResp.class);

        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(stationQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        stationMapper.deleteByPrimaryKey(id);
    }
}
