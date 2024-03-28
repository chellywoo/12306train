package com.lxq.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lxq.train.business.domain.ConformOrder;
import com.lxq.train.business.domain.ConformOrderExample;
import com.lxq.train.business.domain.DailyTrainTicket;
import com.lxq.train.business.enums.ConfirmOrderStatusEnum;
import com.lxq.train.business.mapper.ConformOrderMapper;
import com.lxq.train.business.req.ConformOrderAcceptReq;
import com.lxq.train.business.req.ConformOrderQueryReq;
import com.lxq.train.business.resp.ConformOrderQueryResp;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class ConformOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(ConformOrderService.class);

    @Resource
    private ConformOrderMapper conformOrderMapper;
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    public void save(ConformOrderAcceptReq req){
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

    public void doConfirm(ConformOrderAcceptReq req){
        // 省略业务数据校验，如车次是否存在，余票是否存在，车次是否在有晓琪内，tickets条数>0，同乘客同车次是否已经购买过
        // 做这个的目的是为了防止有人直接调用后端接口

        // 保存数据确认订单表，状态初始化
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();

        DateTime now = new DateTime();
        ConformOrder order = new ConformOrder();
        order.setId(SnowUtil.getSnowFlakeNextId());
        order.setMemberId(LoginMemberContext.getId());
        order.setDate(date);
        order.setTrainCode(trainCode);
        order.setStart(start);
        order.setEnd(end);
        order.setDailyTrainTicketId(req.getDailyTrainTicketId());
        order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setTickets(JSON.toJSONString(req.getTickets()));
        conformOrderMapper.insert(order);

        // 查出余票记录，得到真实的余票信息
        DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(date, trainCode, start, end);
        LOG.info("查出余票记录：{}", dailyTrainTicket);

        // 预扣减余票数据，并判断余票是否充足

        // 选座
            // 一个车厢一个车厢的获取座位数据

            // 挑选符合条件的座位，如果这个车厢不满足，则进入下个车厢，不允许一个订单中存在不同车厢的座位

        // 选中座位后事务处理
            // 座位表修改售卖情况：sell
            // 余票详情修改余票
            // 为用户增加购票记录
            // 更新确认订单表状态为成功
    }
}
