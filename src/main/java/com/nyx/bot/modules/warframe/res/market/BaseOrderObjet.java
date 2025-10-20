package com.nyx.bot.modules.warframe.res.market;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BaseOrderObjet<T> {
    String apiVersion;
    T data;
    Object error;

    @lombok.Data
    @Accessors(chain = true)
    public static class Data<E> {
        String id;
        List<E> items;
    }
}
