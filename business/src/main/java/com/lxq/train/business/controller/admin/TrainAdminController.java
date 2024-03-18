package com.lxq.train.business.controller.admin;

import com.lxq.train.business.req.TrainQueryReq;
import com.lxq.train.business.req.TrainSaveReq;
import com.lxq.train.business.resp.TrainQueryResp;
import com.lxq.train.business.service.TrainSeatService;
import com.lxq.train.business.service.TrainService;
import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/train")
public class TrainAdminController {
    @Resource
    private TrainService trainService;
    @Resource
    private TrainSeatService trainSeatService;
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

    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryResp>> queryAll(){
        List<TrainQueryResp> query = trainService.queryAll();
        return new CommonResp<>(query);
    }

    @GetMapping("/gen-seat/{trainCode}")
    public CommonResp<Object> generateSeat(@PathVariable String trainCode){
        trainSeatService.generateSeat(trainCode);
        return new CommonResp<>();
    }
}
