package com.nyx.bot.draw;

import com.nyx.bot.data.Constant;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.utils.StringUtils;
import io.github.kingprimes.PluginManager;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestHelpDefaultDrawImage {

    PluginManager pm = new PluginManager();

    @Test
    void testHelpDefaultDrawImage() throws IOException {

        List<String> collect = Arrays.stream(Codes.values()).map(c -> StringUtils.removeMatcher(c.getComm())).collect(Collectors.toList());
        byte[] bytes = pm.getFirstPlugin().drawHelpImage(collect);
        ImageIO.write(ImageIO.read(new ByteArrayInputStream(bytes)), Constant.PNG, new File(Constant.DRAW_PATH.formatted("draw_help.png")));
    }

}
