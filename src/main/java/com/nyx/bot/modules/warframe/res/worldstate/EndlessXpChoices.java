package com.nyx.bot.modules.warframe.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EndlessXpChoices {

    @JsonProperty("Category")
    private Category category;
    @JsonProperty("Choices")
    private List<String> choices;

    public enum Category{
        EXC_NORMAL,
        EXC_HARD
    }
}
