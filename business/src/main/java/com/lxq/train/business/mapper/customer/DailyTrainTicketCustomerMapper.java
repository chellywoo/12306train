package com.lxq.train.business.mapper.customer;

import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface DailyTrainTicketCustomerMapper {
    int updateCountBySell(@Param("date") Date date,
                          @Param("trainCode") String trainCode,
                          @Param("seatTypeCode") String seatTypeCode,
                          @Param("minStartIndex") Integer minStartIndex,
                          @Param("maxStartIndex") Integer maxStartIndex,
                          @Param("minEndIndex")Integer minEndIndex,
                          @Param("maxEndIndex")Integer maxEndIndex);
//    int updateCountBySell(Date date, String trainCode, String seatTypeCode, Integer minStartIndex, Integer maxStartIndex, Integer minEndIndex, Integer maxEndIndex);

}