package com.lxq.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.common.req.MemberTicketReq;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import com.lxq.train.member.domain.Ticket;
import com.lxq.train.member.domain.TicketExample;
import com.lxq.train.member.mapper.TicketMapper;
import com.lxq.train.member.req.TicketQueryReq;
import com.lxq.train.member.resp.TicketQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TicketService {
    private static final Logger LOG = LoggerFactory.getLogger(TicketService.class);

    @Resource
    private TicketMapper ticketMapper;

    public void save(MemberTicketReq req) {
        DateTime now = new DateTime();
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        ticket.setId(SnowUtil.getSnowFlakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        ticketMapper.insert(ticket);
    }

    public PageResp<TicketQueryResp> query(TicketQueryReq req){
        TicketExample ticketExample = new TicketExample();
        ticketExample.setOrderByClause("id DESC");
        TicketExample.Criteria criteria = ticketExample.createCriteria();
        if(ObjUtil.isNotEmpty(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }

        LOG.info("查询页数为："+ req.getPage());
        LOG.info("每页条数为："+ req.getSize());
        PageHelper.startPage(req.getPage(),req.getSize());
        List<Ticket> ticketList = ticketMapper.selectByExample(ticketExample);

        PageInfo pageInfo = new PageInfo<>(ticketList);
        LOG.info("总数为："+ pageInfo.getTotal());
        LOG.info("最大分配的页数为："+ pageInfo.getPages());
        List<TicketQueryResp> ticketQueryResp = BeanUtil.copyToList(ticketList, TicketQueryResp.class);

        PageResp<TicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(ticketQueryResp);
        return pageResp;
    }

    public void delete(Long id ){
        ticketMapper.deleteByPrimaryKey(id);
    }
}
