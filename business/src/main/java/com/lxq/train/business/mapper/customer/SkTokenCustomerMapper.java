package com.lxq.train.business.mapper.customer;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface SkTokenCustomerMapper {
    int decrease(@Param("date") Date date, @Param("trainCode") String trainCode, @Param("decreaseCount") int decreaseCount);
}
