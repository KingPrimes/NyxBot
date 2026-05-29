package com.nyx.bot.common.core.dao;


import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
public class BaseEntity {
    protected Integer current = 1;
    protected Integer size = 10;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(current, that.current) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, size);
    }
}
