package com.nyx.bot.res.worldstate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NodeOverride extends BastWorldState{
    private String node;
    private Boolean hide;
    private String levelOverride;
    private DateField activation;
}
