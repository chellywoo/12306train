package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.Station;
import com.lxq.train.business.domain.StationExample;
import com.lxq.train.business.mapper.StationMapper;
import com.lxq.train.business.req.StationQueryReq;
import com.lxq.train.business.req.StationSaveReq;
import com.lxq.train.business.resp.StationQueryResp;
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
public class StationService {
    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;
    public void save(StationSaveReq req){
        DateTime now = new DateTime();
        Station station = BeanUtil.copyProperties(req, Station.class);
        if (ObjectUtil.isNull(station.getId())) {
            // 唯一性判断
            StationExample stationExample = new StationExample();
            StationExample.Criteria criteria = stationExample.createCriteria();
            criteria.andNameEqualTo(req.getName());
            List<Station> stations = stationMapper.selectByExample(stationExample);
            if(CollUtil.isNotEmpty(stations))
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_STATION_UNIQUE_ERROR);
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

    public List<StationQueryResp> queryAll(){
        StationExample stationExample = new StationExample();
        stationExample.setOrderByClause("name_py asc");
        List<Station> stationList = stationMapper.selectByExample(stationExample);
        List<StationQueryResp> stationQueryResp = BeanUtil.copyToList(stationList, StationQueryResp.class);
        return stationQueryResp;
    }
}
