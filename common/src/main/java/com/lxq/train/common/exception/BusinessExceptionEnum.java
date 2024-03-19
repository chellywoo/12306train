package com.lxq.train.common.exception;

public enum BusinessExceptionEnum {
    MEMBER_MOBILE_ALREADY_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取验证码"),
    MEMBER_CODE_ERROR("验证码错误"),
    BUSINESS_STATION_UNIQUE_ERROR("车站已存在"),

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
