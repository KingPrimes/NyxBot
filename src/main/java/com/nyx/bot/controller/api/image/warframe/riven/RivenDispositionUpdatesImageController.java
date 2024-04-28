package com.nyx.bot.controller.api.image.warframe.riven;

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

import java.io.IOException;

/**
 * 紫卡倾向更新列表
 */
@Controller
@RequestMapping("/api")
public class RivenDispositionUpdatesImageController {

    @LogInfo(title = "Api", codes = Codes.WARFRAME_RIVEN_DIS_UPDATE_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/RivenDispositionUpdatesImage", produces = MediaType.IMAGE_PNG_VALUE)
    public void getImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException {
        response.setHeader("content-type", "image/png");
        response.getOutputStream().write(
                HtmlToImage.converse(
                        Constants.LOCALHOST + "private/getRivenDispositionUpdatesHtml"
                ).toByteArray()
        );
    }

}
