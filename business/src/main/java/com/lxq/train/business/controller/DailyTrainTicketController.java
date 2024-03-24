package com.lxq.train.business.controller;

import com.lxq.train.business.req.DailyTrainTicketQueryReq;
import com.lxq.train.business.resp.DailyTrainTicketQueryResp;
import com.lxq.train.business.service.DailyTrainTicketService;
import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/daily-train-ticket")
public class DailyTrainTicketController {
    @Resource
    private DailyTrainTicketService dailyTrainTicketService;
    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req){
        PageResp<DailyTrainTicketQueryResp> query = dailyTrainTicketService.query(req);
        return new CommonResp<>(query);
    }
}
