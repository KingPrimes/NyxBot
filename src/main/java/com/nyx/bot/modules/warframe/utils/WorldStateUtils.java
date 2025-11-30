package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.entity.StateTranslation;
import com.nyx.bot.modules.warframe.entity.exprot.Weapons;
import com.nyx.bot.modules.warframe.enums.FissureTypeEnum;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.repo.exprot.WeaponsRepository;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.StringUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class WorldStateUtils {

    @Resource
    StateTranslationRepository str;

    @Resource
    WeaponsRepository weaponsRepository;

    @Resource
    NodesRepository nodesRepository;

    @Resource
    NightWaveRepository nightwaveRepository;

    /**
     * 获取警报信息列表
     *
     * @return 返回处理后的警报信息列表
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    public List<Alert> getAlerts() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getAlerts().stream()
                .filter(Objects::nonNull)
                .filter(a -> a.getMissionInfo() != null)
                .map(this::translateAlerts)
                .toList();
    }

    /**
     * 翻译警报信息
     * <p>将警报中的物品ID和地点ID转换为对应的名称</p>
     *
     * @param alert 需要翻译的警报对象
     * @return 翻译后的警报对象
     */
    public Alert translateAlerts(Alert alert) {
        List<String> items = alert.getMissionInfo().getMissionReward().getItems();
        if (items != null && !items.isEmpty()) {
            items = items.stream().map(i -> {
                Optional<StateTranslation> name = str.findByUniqueName(i);
                if (name.isPresent()) {
                    return name.get().getName();
                }
                return i;
            }).toList();
            alert.getMissionInfo().getMissionReward().setItems(items);
        }
        String location = alert.getMissionInfo().getLocation();
        if (location != null && !location.isEmpty()) {
            nodesRepository.findById(location).ifPresent(nodes ->
                    alert.getMissionInfo().setLocation(nodes.getName() + "(" + nodes.getSystemName() + ")"));
        }
        return alert;
    }

    /**
     * 获取全站循环信息
     *
     * @return 返回处理后的全站循环信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    public AllCycle getAllCycle() throws DataNotInfoException {
        WorldState warframeStatus = WarframeCache.getWarframeStatus();
        return new AllCycle()
                .setEarthCycle(warframeStatus.getEarthCycle())
                .setCetusCycle(warframeStatus.getCetusCycle())
                .setCambionCycle(warframeStatus.getCambionCycle())
                .setVallisCycle(warframeStatus.getVallisCycle())
                .setZarimanCycle(warframeStatus.getZarimanCycle());
    }

    /**
     * 获取每日特惠信息
     *
     * @return 返回处理后的每日特惠信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    public List<DailyDeals> getDailyDeals() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getDailyDeals()
                .stream()
                .map(this::translateDailyDeals)
                .toList();
    }

    /**
     * 翻译每日特惠信息
     * <p>将每日特惠中的物品ID转换为对应的名称</p>
     *
     * @param dailyDeals 需要翻译的每日特惠对象
     * @return 翻译后的每日特惠对象
     */
    public DailyDeals translateDailyDeals(DailyDeals dailyDeals) {
        str.findByUniqueName(StringUtils.getLastThreeSegments(dailyDeals.getItem())).ifPresent(s -> dailyDeals.setItem(s.getName()));
        return dailyDeals;
    }

    /**
     * 获取双衍王境 轮换信息
     *
     * @return 获取处理后的双衍王境 轮换信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    public DuvalierCycle getDuvalierCycle() throws DataNotInfoException {
        DuvalierCycle duvalierCycle = WarframeCache.getWarframeStatus().getDuvalierCycle();
        return translateDuvalierCycle(duvalierCycle);
    }

    /**
     * 翻译双衍王境 轮换信息
     * <p>将双衍王境 轮换中的物品ID转换为对应的名称</p>
     *
     * @param duvalierCycle 需要翻译的双衍王境 轮换对象
     * @return 翻译后的双衍王境 轮换对象
     */
    public DuvalierCycle translateDuvalierCycle(DuvalierCycle duvalierCycle) {
        List<EndlessXpChoices> list = duvalierCycle.getChoices().stream().peek(c -> {
            if (c.getCategory().equals(EndlessXpChoices.Category.EXC_HARD)) {
                c.setChoices(c.getChoices().stream().map(s ->
                        weaponsRepository.findByEnglishName(s).map(Weapons::getName).orElse(s)
                ).toList());
            }
        }).toList();
        duvalierCycle.setChoices(list);
        return duvalierCycle;
    }

    /**
     * 获取 fissure 信息
     *
     * @param type 获取 fissure 信息的类型
     * @return 返回处理后的fissure信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public List<ActiveMission> getFissure(FissureTypeEnum type) throws DataNotInfoException {
        switch (type) {
            case ACTIVE_MISSION -> {
                return WarframeCache.getWarframeStatus().getActiveMissions().stream()
                        .filter(m -> !m.getHard())
                        .map(this::translateActiveMission)
                        .sorted(Comparator.comparing(ActiveMission::getModifier))
                        .toList();
            }
            case STEEL_PATH -> {
                return WarframeCache.getWarframeStatus().getActiveMissions().stream()
                        .filter(ActiveMission::getHard)
                        .map(this::translateActiveMission)
                        .sorted(Comparator.comparing(ActiveMission::getModifier))
                        .toList();
            }
            case VOID_STORMS -> {
                return WarframeCache.getWarframeStatus().getVoidStorms().stream()
                        .map(v -> {
                            ActiveMission am = new ActiveMission();
                            nodesRepository.findById(v.getNode()).ifPresentOrElse(nodes -> {
                                am.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
                                am.setMissionType(nodes.getMissionType());
                                am.setFaction(nodes.getFactionName());
                            }, () -> am.setNode(v.getNode()));
                            am.set_id(v.get_id());
                            am.setActivation(v.getActivation());
                            am.setExpiry(v.getExpiry());
                            am.setModifier(v.getTier());
                            am.setVoidStorms(true);
                            return am;
                        })
                        .sorted(Comparator.comparing(ActiveMission::getModifier))
                        .toList();
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }

    /**
     * 翻译fissure信息
     * <p>将fissure中的物品ID转换为对应的名称</p>
     *
     * @param activeMission 需要翻译的fissure对象
     * @return 翻译后的fissure对象
     */
    public ActiveMission translateActiveMission(ActiveMission activeMission) {
        nodesRepository.findById(activeMission.getNode()).ifPresent(nodes -> {
            activeMission.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")");
            activeMission.setFaction(nodes.getFactionName());
        });
        return activeMission;
    }

    /**
     * 获取入侵信息
     *
     * @return 获取处理后的入侵信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public List<Invasion> getInvasions() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getInvasions().stream()
                .filter(i -> !i.getCompleted())
                .map(this::translateInvasion).toList();
    }

    /**
     * 翻译入侵信息
     * <p>将入侵中的物品ID转换为对应的名称</p>
     *
     * @param invasion 需要翻译的入侵对象
     * @return 翻译后的入侵对象
     */
    public Invasion translateInvasion(Invasion invasion) {
        nodesRepository.findById(invasion.getNode())
                .ifPresent(nodes -> invasion.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
        List<Reward.Item> items = invasion.getDefenderReward().getCountedItems()
                .stream()
                .filter(Objects::nonNull)
                .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName())))
                .toList();
        invasion.getDefenderReward().setCountedItems(items);

        invasion.setAttackerReward(invasion.getAttackerReward().stream()
                .filter(Objects::nonNull)
                .peek(r -> r.setCountedItems(
                        r.getCountedItems()
                                .stream()
                                .filter(Objects::nonNull)
                                .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName())))
                                .toList()
                )).toList());
        return invasion;
    }

    /**
     * 获取日历信息
     *
     * @return 获取处理后的日历信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("unchecked")
    public List<KnownCalendarSeasons> getKnownCalendarSeasons() throws DataNotInfoException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<KnownCalendarSeasons> originalSeasons = sgs.getKnownCalendarSeasons();
        if (originalSeasons == null || originalSeasons.isEmpty()) {
            throw new DataNotInfoException("KnownCalendarSeasons data is empty");
        }

        String cacheKey = originalSeasons.stream()
                .map(s -> s.getSeason().name() + s.getYearIteration())
                .collect(Collectors.joining("_"));
        // 优先从缓存获取
        @SuppressWarnings("null")
        List<KnownCalendarSeasons> cachedSeasons = CacheUtils.get(CacheUtils.WARFRAME, cacheKey, List.class);
        if (cachedSeasons != null) {
            return cachedSeasons; // 直接使用缓存数据生成图片
        }

        // 缓存未命中时才进行深拷贝和处理
        List<KnownCalendarSeasons> processedSeasons = originalSeasons.stream()
                .map(KnownCalendarSeasons::copy) // 使用优化后的copy方法
                .peek(this::processSeason) // 提取处理逻辑为独立方法
                .toList();
        CacheUtils.set(CacheUtils.WARFRAME, cacheKey, processedSeasons, 300, TimeUnit.MINUTES);
        return processedSeasons;
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

    /**
     * 获取执刑官猎杀信息
     *
     * @return 获取处理后的执刑官猎杀信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public List<LiteSorite> getLiteSorite() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getLiteSorties().stream()
                .peek(s -> s.setMissions(s.getMissions().stream()
                        .peek(v -> nodesRepository.findById(v.getNode())
                                .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"))).toList())).toList();
    }

    /**
     * 获取电波信息
     *
     * @return 获取处理后的电波信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public SeasonInfo getSeasonInfo() throws DataNotInfoException {
        SeasonInfo seasonInfo = WarframeCache.getWarframeStatus().getSeasonInfo();
        seasonInfo.setActiveChallenges(seasonInfo.getActiveChallenges().stream().peek(c ->
                        nightwaveRepository.findById(c.getChallenge()).ifPresent(s -> c.setName(s.getName())
                                .setDescription(s.getDescription())
                                .setStanding(s.getStanding())
                                .setRequired(s.getRequired())
                                .setDaily(s.isDailyTasks())
                                .setWeekly(s.isWeeklyTasks())
                                .setElite(s.isEliteMissions())))
                .toList());
        return seasonInfo;
    }

    /**
     * 获取突击信息
     *
     * @return 获取处理后的突击信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public List<Sortie> getSorties() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getSorties().stream()
                .peek(s -> s.setVariants(s.getVariants().stream()
                        .peek(v -> nodesRepository.findById(v.getNode())
                                .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"))).toList())).toList();
    }

    /**
     * 获取虚空商人信息
     *
     * @return 获取处理后的虚空商人信息
     * @throws DataNotInfoException 当无法获取到世界状态数据时抛出异常
     */
    @SuppressWarnings("null")
    public List<VoidTrader> getVoidTraders() throws DataNotInfoException {
        return WarframeCache.getWarframeStatus().getVoidTraders().stream()
                .map(this::translateVoidTraders).toList();
    }

    /**
     * 翻译虚空商人信息
     * <p>将虚空商人中的地点ID和商品名称ID转换为对应的实际名称</p>
     *
     * @param voidTrader 需要翻译的虚空商人对象
     * @return 翻译后的虚空商人对象
     */
    public VoidTrader translateVoidTraders(VoidTrader voidTrader) {
        // 翻译虚空商人出现地点名称
        nodesRepository.findById(voidTrader.getNode())
                .ifPresent(nodes -> voidTrader.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
        // 翻译商品清单中的物品名称
        if (voidTrader.getManifest() != null && !voidTrader.getManifest().isEmpty()) {
            voidTrader.setManifest(voidTrader.getManifest()
                    .stream()
                    .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName())))
                    .toList()
            );
        }
        return voidTrader;
    }
}
