package com.nyx.bot.core.dao;


import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.Views;
import lombok.Data;


@Data

public class BaseEntity {
    @JsonView(Views.BaseView.class)
    protected Integer current = 1;
    @JsonView(Views.BaseView.class)
    protected Integer size = 10;

}
