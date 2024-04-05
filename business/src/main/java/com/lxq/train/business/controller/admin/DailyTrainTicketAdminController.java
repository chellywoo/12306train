package com.lxq.train.business.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.business.req.DailyTrainTicketQueryReq;
import com.lxq.train.business.req.DailyTrainTicketSaveReq;
import com.lxq.train.business.resp.DailyTrainTicketQueryResp;
import com.lxq.train.business.service.DailyTrainTicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/daily-train-ticket")
public class DailyTrainTicketAdminController {
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody DailyTrainTicketSaveReq req){
        dailyTrainTicketService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req){
        PageResp<DailyTrainTicketQueryResp> query = dailyTrainTicketService.query(req);
        return new CommonResp<>(query);
    }
    @GetMapping("/query-list2")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList2(@Valid DailyTrainTicketQueryReq req){
        PageResp<DailyTrainTicketQueryResp> query = dailyTrainTicketService.query2(req);
        return new CommonResp<>(query);
    }
//    @GetMapping("/query-list3")
//    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList3(@Valid DailyTrainTicketQueryReq req){
//        PageResp<DailyTrainTicketQueryResp> query = dailyTrainTicketService.query3(req);
//        return new CommonResp<>(query);
//    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        dailyTrainTicketService.delete(id);
        return new CommonResp<>();
    }
}
