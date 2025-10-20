package com.nyx.bot.modules.warframe.res.market;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BaseOrder<T> {
    String apiVersion;
    List<T> data;
    Object error;
}
