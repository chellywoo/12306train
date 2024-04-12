package com.lxq.train.business.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.lxq.train.business.enums.RedisKeyEnum;
import com.lxq.train.business.enums.RocketMQTopicEnum;
import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.common.context.LoginMemberContext;
import com.lxq.train.common.exception.BusinessException;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BeforeConfirmOrderService {
    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Resource
    private SkTokenService skTokenService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @SentinelResource(value="beforeDoConfirm",blockHandler = "beforeDoConfirmBlock")
    public void beforeDoConfirm(ConfirmOrderAcceptReq req) {
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

        String key = RedisKeyEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(req.getDate()) + "-" + req.getTrainCode();
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(key, key, 2, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(setIfAbsent)) {
            LOG.info("恭喜抢到锁了，lockKey:{}", key);
        } else {
            // 只是没抢到锁，之后还要继续操作的
            LOG.info("很遗憾没抢到锁，lockKey:{}", key);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_ERROR);
        }

        // 可以购票：TODO 发送MQ，准备出票
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
