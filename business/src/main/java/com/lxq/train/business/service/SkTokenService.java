package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.SkToken;
import com.lxq.train.business.domain.SkTokenExample;
import com.lxq.train.business.mapper.SkTokenMapper;
import com.lxq.train.business.req.SkTokenQueryReq;
import com.lxq.train.business.req.SkTokenSaveReq;
import com.lxq.train.business.resp.SkTokenQueryResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class SkTokenService {
    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);
    @Resource
    private DailyTrainSeatService dailyTrainSeatService;
    @Resource
    private DailyTrainStationService dailyTrainStationService;
    @Resource
    private SkTokenMapper skTokenMapper;
    public void save(SkTokenSaveReq req){
        DateTime now = new DateTime();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowFlakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        }else{
            skToken.setUpdateTime(now);
            skTokenMapper.updateByPrimaryKey(skToken);
        }
    }

    public PageResp<SkTokenQueryResp> query(SkTokenQueryReq req){
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.setOrderByClause("id DESC");
        SkTokenExample.Criteria criteria = skTokenExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<SkToken> skTokenList = skTokenMapper.selectByExample(skTokenExample);

        PageInfo pageInfo = new PageInfo<>(skTokenList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<SkTokenQueryResp> skTokenQueryResp = BeanUtil.copyToList(skTokenList, SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(skTokenQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        skTokenMapper.deleteByPrimaryKey(id);
    }

    public void generateDaily(Date date, String trainCode){
        LOG.info("开始生成【{}】日车次【{}】令牌数据", DateUtil.formatDate(date),trainCode);

        //删除某日某车次的令牌信息
        SkTokenExample skTokenExample = new SkTokenExample();
        skTokenExample.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        skTokenMapper.deleteByExample(skTokenExample);
        DateTime now = new DateTime();
        SkToken skToken = new SkToken();
        skToken.setId(SnowUtil.getSnowFlakeNextId());
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);

        int seatCount = dailyTrainSeatService.countByTrainCode(date, trainCode);
        LOG.info("车次【{}】座位数为：{}", trainCode, seatCount);

        long stationCount = dailyTrainStationService.countByTrainCode(date, trainCode);
        LOG.info("车次【{}】车站数为：{}", trainCode, stationCount);

        int skCount = (int) (seatCount * stationCount * 3/4);
        LOG.info("车次【{}】初始生成令牌数为：{}", trainCode, skCount);
        skToken.setCount(skCount);
        skTokenMapper.insert(skToken);
    }
}
