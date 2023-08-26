package com.nyx.bot.core.dao;


import lombok.Getter;

@Getter
public class BaseEntity {
    protected Integer pageNum;
    protected Integer pageSize = 10;
    protected Integer totalPage;
    protected Integer totalCount;

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

}
