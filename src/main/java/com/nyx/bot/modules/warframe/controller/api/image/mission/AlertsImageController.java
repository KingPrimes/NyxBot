package com.nyx.bot.modules.warframe.controller.api.image.mission;

import com.nyx.bot.annotation.LogInfo;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.core.Constants;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.BusinessType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.Alert;
import com.nyx.bot.utils.HtmlToImage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * 警报
 */
@Controller
@RequestMapping("/api")
public class AlertsImageController {

    @LogInfo(title = "Api", codes = Codes.WARFRAME_ALERTS_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/getAlertsImage", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void getImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException, DataNotInfoException {
        response.setHeader("content-type", "image/png");

        response.getOutputStream().write(
                HtmlToImage.generateImage(
                        "html/alerts",
                        () -> {
                            WorldState sgs;
                            try {
                                sgs = WarframeCache.getWarframeStatus();
                            } catch (DataNotInfoException e) {
                                throw new RuntimeException(e);
                            }
                            List<Alert> alerts = sgs.getAlerts();

                            ModelMap model = new ModelMap();
                            model.put("alerts", alerts.isEmpty() ? null : alerts);
                            return model;
                        }
                ).toByteArray()
        );
    }

    @LogInfo(title = "订阅", codes = Codes.WARFRAME_ALERTS_PLUGIN, businessType = BusinessType.IMAGE)
    @PostMapping(value = "/postSubAlertsImage", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void postImage(HttpServletResponse response, @RequestBody OneBotLogInfoData data) throws IOException, HtmlToImageException {
        response.setHeader("content-type", "image/png");
        response.getOutputStream().write(
                HtmlToImage.conversePost(
                        Constants.LOCALHOST + "private/getAlertsHtml",
                        data.getData()
                ).toByteArray()
        );
    }
}
