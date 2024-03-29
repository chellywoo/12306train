package com.lxq.train.business.controller;

import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.business.service.ConfirmOrderService;
import com.lxq.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {
    @Resource
    private ConfirmOrderService confirmOrderService;
    @PostMapping("/accept")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderAcceptReq req){
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }
}
