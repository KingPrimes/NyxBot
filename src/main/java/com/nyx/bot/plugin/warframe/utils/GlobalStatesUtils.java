package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.GlobalStates;

import java.util.List;
import java.util.stream.Collectors;

public class GlobalStatesUtils {
    /**
     * 取差集
     *
     * @param old 老数据
     * @param now 新数据
     * @return 差集数据
     */
    public static <T extends GlobalStates.BaseStatus> List<T> takeTheDifferenceSet(List<T> old, List<T> now) {
        //取差集
        return now.stream()
                // 过滤任务是否正在运行
                .filter(GlobalStates.BaseStatus::getActive)
                // 取新任务与旧任务的差值
                .filter(item ->
                        !old.stream()
                                //采用Map Key的方式对比多属性不同的值
                                .collect(
                                        Collectors.toMap(
                                                GlobalStates.BaseStatus::getId
                                                , value -> value)
                                )
                                .containsKey(item.getId())

                )
                .toList();
    }
}
