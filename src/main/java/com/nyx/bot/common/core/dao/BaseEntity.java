package com.nyx.bot.common.core.dao;


import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
public class BaseEntity {
    /**
     * 当前页码（仅前端传参用，不入库）
     */
    @Transient
    protected Integer current = 1;
    /**
     * 每页条数（仅前端传参用，不入库）
     */
    @Transient
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
