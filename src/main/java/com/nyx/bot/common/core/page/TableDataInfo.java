package com.nyx.bot.common.core.page;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
@JsonView(Views.View.class)
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

    public TableDataInfo(int code, long total, long size, List<?> content) {
        this.code = code;
        this.data = new Data();
        this.data.setTotal(total);
        this.data.setSize(size);
        this.data.setRecords(content);
    }

    @lombok.Data
    @JsonView(Views.View.class)
    public static class Data {

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
        private List<?> records;

    }
}