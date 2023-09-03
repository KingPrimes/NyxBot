package com.nyx.bot.core.dao;


import lombok.Data;

@Data
public class BaseEntity {
    protected Integer pageNum = 1;
    protected Integer pageSize = 10;
    protected Integer totalPage = 0;
    protected Integer totalCount = 0;
}
