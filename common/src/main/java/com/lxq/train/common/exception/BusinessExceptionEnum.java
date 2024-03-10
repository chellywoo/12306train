package com.lxq.train.common.exception;

public enum BusinessExceptionEnum {
    MOBILE_ALREADY_EXIST("手机号已注册"),
    MOBILE_NOT_EXIST("请先获取验证码"),
    CODE_ERROR("验证码错误"),

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
