package com.lxq.train.member.controller.admin;

import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.common.resp.PageResp;
import com.lxq.train.member.req.TicketQueryReq;
import com.lxq.train.member.req.TicketSaveReq;
import com.lxq.train.member.resp.TicketQueryResp;
import com.lxq.train.member.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/ticket")
public class TicketAdminController {
    @Resource
    private TicketService ticketService;
    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody TicketSaveReq req){
        ticketService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<TicketQueryResp>> queryList(@Valid TicketQueryReq req){
        PageResp<TicketQueryResp> query = ticketService.query(req);
        return new CommonResp<>(query);
    }
    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id){
        ticketService.delete(id);
        return new CommonResp<>();
    }
}
