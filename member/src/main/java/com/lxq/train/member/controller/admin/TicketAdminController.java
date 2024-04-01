package com.lxq.train.member.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.member.req.TicketQueryReq;
import com.lxq.train.member.resp.TicketQueryResp;
import com.lxq.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {
    @Resource
    private TicketService ticketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req){
        PageResp<TicketQueryResp> query = ticketService.query(req);
        return new CommonResp<>(query);
    }
}
