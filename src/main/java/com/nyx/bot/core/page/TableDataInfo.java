package com.nyx.bot.core.page;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
public class TableDataInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 消息状态码
     */
    private int code;

    /**
     * 消息内容
     */
    private String msg;

    private Data data;

    /**
     * 表格数据对象
     */
    public TableDataInfo() {
    }

    public TableDataInfo(int code, long totalElements, long totalPages, long size, List<?> content) {
        this.code = code;
        this.data = new Data();
        this.data.setTotalElements(totalElements);
        this.data.setTotalPages(totalPages);
        this.data.setSize(size);
        this.data.setContent(content);
    }

    @lombok.Data
    public static class Data {
        /**
         * 总记录数
         */
        private long totalElements;

        /**
         * 总页数
         */
        private long totalPages;

        /**
         * 每页记录数
         */
        private long size;

        /**
         * 列表数据
         */
        private List<?> content;

    }
}