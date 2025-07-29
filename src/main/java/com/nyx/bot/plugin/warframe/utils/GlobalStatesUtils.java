package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.worldstate.BastWorldState;

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
    public static <T extends BastWorldState> List<T> takeTheDifferenceSet(List<T> old, List<T> now) {
        //取差集
        return now.stream()
                // 取新任务与旧任务的差值
                .filter(item ->
                        !old.stream()
                                //采用Map Key的方式对比多属性不同的值
                                .collect(
                                        Collectors.toMap(
                                                BastWorldState::get_id
                                                , value -> value)
                                )
                                .containsKey(item.get_id())

                )
                .toList();
    }
}
