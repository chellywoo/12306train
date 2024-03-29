package com.lxq.train.business.controller.admin;

import com.lxq.train.business.req.ConfirmOrderQueryReq;
import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.ConfirmOrderAcceptReq;
import com.lxq.train.business.resp.ConfirmOrderQueryResp;
import com.lxq.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderAdminController {
    @Resource
    private ConfirmOrderService confirmOrderService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody ConfirmOrderAcceptReq req){
        confirmOrderService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<ConfirmOrderQueryResp>> queryList(@Valid ConfirmOrderQueryReq req){
        PageResp<ConfirmOrderQueryResp> query = confirmOrderService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        confirmOrderService.delete(id);
        return new CommonResp<>();
    }
}
