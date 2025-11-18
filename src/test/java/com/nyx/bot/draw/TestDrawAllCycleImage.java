package com.nyx.bot.draw;

import com.nyx.bot.data.Constant;
import io.github.kingprimes.PluginManager;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.AllCycle;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TestDrawAllCycleImage {

    PluginManager pm = new PluginManager();

    @Test
    void testDrawAllCycleImage() throws IOException {
        WorldState worldState = Constant.WORLD_STATE;
        AllCycle allCycle = new AllCycle()
                .setEarthCycle(worldState.getEarthCycle())
                .setCetusCycle(worldState.getCetusCycle())
                .setCambionCycle(worldState.getCambionCycle())
                .setVallisCycle(worldState.getVallisCycle())
                .setZarimanCycle(worldState.getZarimanCycle());
        byte[] bytes = pm.getFirstPlugin().drawAllCycleImage(allCycle);
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(bytes)), Constant.PNG, new File(Constant.DRAW_PATH.formatted("draw_all_cycle.png")));
    }
}
