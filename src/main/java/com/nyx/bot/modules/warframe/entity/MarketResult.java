package com.nyx.bot.modules.warframe.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MarketResult<E, R> {
    E entity;
    R result;
    List<String> possibleItems;
}
