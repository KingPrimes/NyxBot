package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.KnownCalendarSeasons;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Shiro
@Component
@Slf4j
public class KnownCalendarSeasonsPlugin {

    private final ConcurrentHashMap<String, List<KnownCalendarSeasons>> seasonCache = new ConcurrentHashMap<>();
    @Resource
    StateTranslationRepository str;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_KNOWN_CALENDAR_SEASONS_CMD, at = AtEnum.BOTH)
    public void knownCalendarSeasonsHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postKnownCalendarSeasonsImage(), Codes.WARFRAME_KNOWN_CALENDAR_SEASONS_PLUGIN, log);
    }


    private byte[] postKnownCalendarSeasonsImage() throws DataNotInfoException, HtmlToImageException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<KnownCalendarSeasons> originalSeasons = sgs.getKnownCalendarSeasons();
        if (originalSeasons == null || originalSeasons.isEmpty()) {
            throw new DataNotInfoException("KnownCalendarSeasons data is empty");
        }

        String cacheKey = originalSeasons.stream()
                .map(s -> s.getSeason().name() + s.getYearIteration())
                .collect(Collectors.joining("_"));
        // 优先从缓存获取
        List<KnownCalendarSeasons> cachedSeasons = seasonCache.get(cacheKey);
        if (cachedSeasons != null) {
            return generateImage(cachedSeasons); // 直接使用缓存数据生成图片
        }

        // 缓存未命中时才进行深拷贝和处理
        List<KnownCalendarSeasons> processedSeasons = originalSeasons.stream()
                .map(KnownCalendarSeasons::copy) // 使用优化后的copy方法
                .peek(this::processSeason) // 提取处理逻辑为独立方法
                .toList();

        // 存入缓存（设置短期过期时间，避免数据陈旧）
        seasonCache.put(cacheKey, processedSeasons);

        return generateImage(processedSeasons);
    }


    private void processSeason(KnownCalendarSeasons season) {
        season.processDays();
        season.setMonthDays(season.getDays().stream()
                .peek(day -> day.setEvents(
                        day.getEvents().stream()
                                .peek(this::processEvent)
                                .collect(Collectors.toList())
                )).collect(Collectors.groupingBy(KnownCalendarSeasons.Days::getMonth))
        );
        season.setDays(null);
    }


    private void processEvent(KnownCalendarSeasons.Events event) {
        switch (event.getType()) {
            case CET_CHALLENGE -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getChallenge()))
                    .ifPresent(s -> event.setChallengeInfo(new KnownCalendarSeasons.Events.Challenge(s.getName(), s.getDescription())));

            case CET_REWARD -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getReward()))
                    .ifPresent(s -> event.setReward(StringUtils.deleteBetweenAndMarkers(s.getName(), '<', '>')));

            case CET_UPGRADE -> str.findByUniqueName(StringUtils.getLastThreeSegments(event.getUpgrade()))
                    .ifPresent(s -> event.setUpgradeInfo(new KnownCalendarSeasons.Events.Upgrade(s.getName(), s.getDescription())));

        }
    }

    private byte[] generateImage(List<KnownCalendarSeasons> seasons) throws HtmlToImageException, DataNotInfoException {
        return HtmlToImage.generateImage("html/knownCalendarSeasons", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("seasons", seasons);
            return modelMap;
        }).toByteArray();
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    protected void scheduleCacheCleanup() {
        seasonCache.clear();
    }

}
