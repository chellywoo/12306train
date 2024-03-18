package com.lxq.train.business.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.TrainQueryReq;
import com.lxq.train.business.req.TrainSaveReq;
import com.lxq.train.business.resp.TrainQueryResp;
import com.lxq.train.business.service.TrainService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train")
public class TrainAdminController {
    @Resource
    private TrainService trainService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody TrainSaveReq req){
        trainService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<TrainQueryResp>> queryList(@Valid TrainQueryReq req){
        PageResp<TrainQueryResp> query = trainService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainService.delete(id);
        return new CommonResp<>();
    }
}
