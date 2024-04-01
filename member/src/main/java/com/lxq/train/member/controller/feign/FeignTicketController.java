package com.lxq.train.member.controller.feign;

import com.lxq.train.common.req.MemberTicketReq;
import com.lxq.train.common.resp.CommonResp;
import com.lxq.train.member.service.TicketService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/ticket")
public class FeignTicketController {
    @Resource
    private TicketService ticketService;

    /**
     * 用户购买车票后保存
     * @param req
     * @return
     */
    @PostMapping ("/save")
    public CommonResp<Object> save(@RequestBody @Validated MemberTicketReq req){
        ticketService.save(req);
        return new CommonResp<>();
    }

}
