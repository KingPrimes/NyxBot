package com.nyx.bot.controller.api.image.warframe.mission;

import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.core.Constants;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.exception.HtmlToImageException;
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
public class FissuresImageController {
    @LogInfo(title = "Api", codes = Codes.WARFRAME_FISSURES_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/postFissuresImage", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void postFissuresImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException {
        response.setHeader("content-type", "image/png");
        int i;
        switch (data.getCodes()) {
            case WARFRAME_FISSURES_EMPYREAN_PLUGIN -> i = 1;
            case WARFRAME_FISSURES_PATH_PLUGIN -> i = 2;
            default -> i = 0;
        }
        response.getOutputStream().write(
                HtmlToImage.converse(
                        Constants.LOCALHOST + "private/getFissuresHtml/" + i
                ).toByteArray()
        );
    }

    @LogInfo(title = "Api订阅", codes = Codes.WARFRAME_FISSURES_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/postSubscribeFissuresImage", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void postSubscribeFissuresImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException {
        response.setHeader("content-type", "image/png");
        response.getOutputStream().write(
                HtmlToImage.conversePost(
                        Constants.LOCALHOST + "private/postSubscribeFissuresHtml",
                        data.getData()
                ).toByteArray()
        );
    }
}
