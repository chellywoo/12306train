package com.lxq.train.common.req;

import jakarta.validation.constraints.NotNull;

public class PageReq {
    @NotNull(message = "页码不能为空")
    private Integer page;
    @NotNull(message = "每页条数不能为空")
//    @Max(value = 15, message = "最大值不能超过15条")
    private Integer size;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PageReq{");
        sb.append("page=").append(page);
        sb.append(", size=").append(size);
        sb.append('}');
        return sb.toString();
    }
}
