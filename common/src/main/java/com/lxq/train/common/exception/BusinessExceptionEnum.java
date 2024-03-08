package com.lxq.train.common.exception;

public enum BusinessExceptionEnum {
    MOBILE_ALREADY_EXIST("手机号已注册"),
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
