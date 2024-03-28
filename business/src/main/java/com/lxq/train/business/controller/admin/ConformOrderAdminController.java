package com.lxq.train.business.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.ConformOrderQueryReq;
import com.lxq.train.business.req.ConformOrderSaveReq;
import com.lxq.train.business.resp.ConformOrderQueryResp;
import com.lxq.train.business.service.ConformOrderService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/confirm-order")
public class ConformOrderAdminController {
    @Resource
    private ConformOrderService conformOrderService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody ConformOrderSaveReq req){
        conformOrderService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<ConformOrderQueryResp>> queryList(@Valid ConformOrderQueryReq req){
        PageResp<ConformOrderQueryResp> query = conformOrderService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        conformOrderService.delete(id);
        return new CommonResp<>();
    }
}
