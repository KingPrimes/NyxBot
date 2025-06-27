package com.nyx.bot.res.worldstate;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SyndicateMission extends BastWorldState{
    private DateField activation;
    private DateField expiry;
    private String tag;
    private Integer seed;
    private List<String> nodes;
    private List<Job> jobs;
}
