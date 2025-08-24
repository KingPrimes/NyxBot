package com.nyx.bot.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    /**
     * 将列表拆分为批次
     *
     * @param list      列表
     * @param batchSize 批次大小
     */
    public static <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }

}
