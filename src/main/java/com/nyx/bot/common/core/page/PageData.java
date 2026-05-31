package com.nyx.bot.common.core.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 每页记录数
     */
    private long size;

    /**
     * 当前页数
     */
    private long current;

    /**
     * 列表数据
     */
    private List<T> records;
}
