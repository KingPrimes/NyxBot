package com.nyx.bot.res.worldstate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyx.bot.res.enums.SyndicateEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SyndicateMission extends BastWorldState{
    @JsonProperty("Tag")
    private SyndicateEnum tag;
    @JsonProperty("Seed")
    private Integer seed;
    @JsonProperty("Nodes")
    private List<String> nodes;
    @JsonProperty("Jobs")
    private List<Job> jobs;
}
