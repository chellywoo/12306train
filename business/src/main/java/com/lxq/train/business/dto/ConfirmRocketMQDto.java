package com.lxq.train.business.dto;

import java.util.Date;

public class ConfirmRocketMQDto {

    /**
     * 日志流水号
     */
    private String logId;

    /**
     * 日期
     */
    private Date date;

    /**
     * 车次编号
     */
    private String trainCode;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    @Override
    public String toString() {
        return "ConfirmRocketMQDto{" +
                "logId='" + logId + '\'' +
                ", date=" + date +
                ", trainCode='" + trainCode + '\'' +
                '}';
    }
}
