package com.lxq.train.common.exception;

public enum BusinessExceptionEnum {
    MEMBER_MOBILE_ALREADY_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取验证码"),
    MEMBER_CODE_ERROR("验证码错误"),
    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("该车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("该车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("该车次厢号已存在"),


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
