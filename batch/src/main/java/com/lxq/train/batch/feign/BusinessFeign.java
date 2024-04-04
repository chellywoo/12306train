package com.lxq.train.batch.feign;

import com.lxq.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

@FeignClient("business")
//@FeignClient(name = "business", url = "http://localhost:8000/business")
public interface BusinessFeign {

    @GetMapping("/business/hello")
    String hello();

    @GetMapping("/business/admin/daily-train/generate-daily/{date}")
    CommonResp<Object> generateDaily(@DateTimeFormat(pattern = "yyyy-MM-dd") @PathVariable("date") Date date);
}

