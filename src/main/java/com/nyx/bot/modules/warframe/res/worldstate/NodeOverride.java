package com.nyx.bot.modules.warframe.res.worldstate;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NodeOverride extends BastWorldState{
    private String node;
    private Boolean hide;
    private String levelOverride;
    private DateField activation;
}
