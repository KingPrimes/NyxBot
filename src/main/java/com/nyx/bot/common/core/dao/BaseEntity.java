package com.nyx.bot.common.core.dao;


import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.common.core.Views;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
public class BaseEntity {
    @JsonView(Views.BaseView.class)
    protected Integer current = 1;
    @JsonView(Views.BaseView.class)
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
