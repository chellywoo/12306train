package com.lxq.train.business.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.TrainSeatQueryReq;
import com.lxq.train.business.req.TrainSeatSaveReq;
import com.lxq.train.business.resp.TrainSeatQueryResp;
import com.lxq.train.business.service.TrainSeatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train-seat")
public class TrainSeatAdminController {
    @Resource
    private TrainSeatService trainSeatService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody TrainSeatSaveReq req){
        trainSeatService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<TrainSeatQueryResp>> queryList(@Valid TrainSeatQueryReq req){
        PageResp<TrainSeatQueryResp> query = trainSeatService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        trainSeatService.delete(id);
        return new CommonResp<>();
    }
}
