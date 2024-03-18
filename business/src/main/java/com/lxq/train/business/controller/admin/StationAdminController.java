package com.lxq.train.business.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.StationQueryReq;
import com.lxq.train.business.req.StationSaveReq;
import com.lxq.train.business.resp.StationQueryResp;
import com.lxq.train.business.service.StationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/station")
public class StationAdminController {
    @Resource
    private StationService stationService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody StationSaveReq req){
        stationService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<StationQueryResp>> queryList(@Valid StationQueryReq req){
        PageResp<StationQueryResp> query = stationService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        stationService.delete(id);
        return new CommonResp<>();
    }
}
