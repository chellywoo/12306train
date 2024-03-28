package com.lxq.train.business.controller;

import com.lxq.train.business.req.ConformOrderAcceptReq;
import com.lxq.train.business.service.ConformOrderService;
import com.lxq.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConformOrderController {
    @Resource
    private ConformOrderService conformOrderService;
    @PostMapping("/accept")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConformOrderAcceptReq req){
        conformOrderService.doConfirm(req);
        return new CommonResp<>();
    }
}
