package com.nyx.bot.modules.warframe.controller.api.image.mission;

import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.utils.HtmlToImage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/api")
public class NighWaveImageController {
    @LogInfo(title = "Api", codes = Codes.WARFRAME_NIGH_WAVE_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/postNighWaveImage", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void getImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException {
        response.setHeader("content-type", "image/png");
        response.getOutputStream().write(
                HtmlToImage.converse(
                        Constants.LOCALHOST + "private/getNighWaveHtml"
                ).toByteArray()
        );
    }
}
