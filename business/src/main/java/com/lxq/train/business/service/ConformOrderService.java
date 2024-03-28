package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.business.domain.ConformOrder;
import com.lxq.train.business.domain.ConformOrderExample;
import com.lxq.train.business.mapper.ConformOrderMapper;
import com.lxq.train.business.req.ConformOrderQueryReq;
import com.lxq.train.business.req.ConformOrderSaveReq;
import com.lxq.train.business.resp.ConformOrderQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ConformOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(ConformOrderService.class);

    @Resource
    private ConformOrderMapper conformOrderMapper;
    public void save(ConformOrderSaveReq req){
        DateTime now = new DateTime();
        ConformOrder conformOrder = BeanUtil.copyProperties(req, ConformOrder.class);
        if (ObjectUtil.isNull(conformOrder.getId())) {
            conformOrder.setId(SnowUtil.getSnowFlakeNextId());
            conformOrder.setCreateTime(now);
            conformOrder.setUpdateTime(now);
            conformOrderMapper.insert(conformOrder);
        }else{
            conformOrder.setUpdateTime(now);
            conformOrderMapper.updateByPrimaryKey(conformOrder);
        }
    }

    public PageResp<ConformOrderQueryResp> query(ConformOrderQueryReq req){
        ConformOrderExample conformOrderExample = new ConformOrderExample();
        conformOrderExample.setOrderByClause("id DESC");
        ConformOrderExample.Criteria criteria = conformOrderExample.createCriteria();

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<ConformOrder> conformOrderList = conformOrderMapper.selectByExample(conformOrderExample);

        PageInfo pageInfo = new PageInfo<>(conformOrderList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<ConformOrderQueryResp> conformOrderQueryResp = BeanUtil.copyToList(conformOrderList, ConformOrderQueryResp.class);

        PageResp<ConformOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(conformOrderQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        conformOrderMapper.deleteByPrimaryKey(id);
    }
}
