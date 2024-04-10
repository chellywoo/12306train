package com.lxq.train.batch.feign;

import com.lxq.train.common.resp.CommonResp;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BusinessFeignFallBack implements BusinessFeign{

    @Override
    public String hello() {
        return "FallBack";
    }

    @Override
    public CommonResp<Object> generateDaily(Date date) {
        return null;
    }
}
