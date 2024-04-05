package com.lxq.train.common.exception;

public enum BusinessExceptionEnum {
    MEMBER_MOBILE_ALREADY_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取验证码"),
    MEMBER_CODE_ERROR("验证码错误"),
    MEMBER_PASSENGER_MAX("添加的乘客数已到限定值"),
    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("该车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("该车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("该车次厢号已存在"),
    CONFIRM_ORDER_YDZ_TICKET_COUNT_ERROR("一等座余票不足"),
    CONFIRM_ORDER_EDZ_TICKET_COUNT_ERROR("二等座余票不足"),
    CONFIRM_ORDER_RW_TICKET_COUNT_ERROR("软卧余票不足"),
    CONFIRM_ORDER_YW_TICKET_COUNT_ERROR("硬卧余票不足"),
    CONFIRM_ORDER_EXCEPTION("服务器繁忙，请稍后重试"),

    ;
    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
