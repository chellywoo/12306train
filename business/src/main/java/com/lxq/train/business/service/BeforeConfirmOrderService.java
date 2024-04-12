package com.lxq.train.business.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.lxq.train.business.domain.ConfirmOrder;
import com.lxq.train.business.enums.ConfirmOrderStatusEnum;
import com.lxq.train.business.enums.RocketMQTopicEnum;
import com.lxq.train.business.mapper.ConfirmOrderMapper;
import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.business.req.ConfirmOrderTicketReq;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BeforeConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private SkTokenService skTokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @SentinelResource(value="beforeDoConfirm",blockHandler = "beforeDoConfirmBlock")
    public void beforeDoConfirm(ConfirmOrderAcceptReq req) {
        req.setMemberId(LoginMemberContext.getId());

        // 保存数据确认订单表，状态初始化
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        DateTime now = new DateTime();
        ConfirmOrder order = new ConfirmOrder();
        order.setId(SnowUtil.getSnowFlakeNextId());
        order.setMemberId(req.getMemberId());
        order.setDate(date);
        order.setTrainCode(trainCode);
        order.setStart(start);
        order.setEnd(end);
        order.setDailyTrainTicketId(req.getDailyTrainTicketId());
        order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setTickets(JSON.toJSONString(tickets));
        confirmOrderMapper.insert(order);

        //校验令牌余量
        int validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
        if (validSkToken > 0)
            LOG.info("令牌校验通过");
        else if (validSkToken == 0) {
            LOG.info("未通过令牌校验");
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_ERROR);
        } else {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_EXCEPTION);
        }

        // 可以购票：
        req.setLogId(MDC.get("LOG_ID"));
        String reqJson = JSON.toJSONString(req);
        LOG.info("排队购票，发送MQ消息，消息内容：{}", reqJson);
        rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(),reqJson);
        LOG.info("排队购票，发送MQ消息结束");
    }

    //降级处理
    public void beforeDoConfirmBlock(ConfirmOrderAcceptReq req, BlockException e){
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}
