package com.lxq.train.business.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.business.service.ConfirmOrderService;
import com.lxq.train.common.exception.BusinessExceptionEnum;
import com.lxq.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {
    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);
    @Resource
    private ConfirmOrderService confirmOrderService;

    // 接口资源名称不能与接口路径一致，会导致限流后走不到降级的方法中
    @SentinelResource(value="confirmOrderAccept",blockHandler = "doConfirmBlock")
    @PostMapping("/accept")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderAcceptReq req){
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

    /**
     * 降级方法，需要包含限流方法的所有参数和BlockException参数，返回值也要与原接口保持一致
     * @param req
     * @param e
     * @return
     */
    public CommonResp<Object> doConfirmBlock(ConfirmOrderAcceptReq req, BlockException e){
        LOG.info("购票请求被限流：{}", req);
//        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
        CommonResp<Object> commonResp = new CommonResp<>();
        commonResp.setSuccess(false);
        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return commonResp;
    }
}
