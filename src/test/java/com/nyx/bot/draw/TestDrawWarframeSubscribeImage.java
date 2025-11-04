package com.nyx.bot.draw;

import com.nyx.bot.data.Constant;
import com.nyx.bot.modules.warframe.utils.WarframeSubscribeCheck;
import io.github.kingprimes.PluginManager;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class TestDrawWarframeSubscribeImage {

    PluginManager pm = new PluginManager();

    @Test
    void testDrawWarframeSubscribeImage() throws IOException {
        Map<Integer, String> subscribeEnums = WarframeSubscribeCheck.getSubscribeEnums();
        Map<Integer, String> subscribeMissionTypeEnums = WarframeSubscribeCheck.getSubscribeMissionTypeEnums();
        byte[] bytes = pm.getFirstPlugin().drawWarframeSubscribeImage(subscribeEnums, subscribeMissionTypeEnums);
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(bytes)), Constant.PNG, new File(Constant.DRAW_PATH.formatted("draw_warframe_subscribe.png")));
    }

}
