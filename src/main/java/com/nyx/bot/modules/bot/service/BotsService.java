package com.nyx.bot.modules.bot.service;

import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.bot.service.black.BlackService;
import com.nyx.bot.modules.bot.service.white.WhiteService;
import com.nyx.bot.utils.I18nUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BotsService {
    @Resource
    BotContainer container;

    @Resource
    WhiteService ws;

    @Resource
    BlackService bs;

    /***
     * 获取机器人列表
     */
    public AjaxResult getBots() {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        return AjaxResult.success().put("data", container.robots.keySet().stream().map(k -> Map.of("label", container.robots.get(k).getLoginInfo().getData().getNickname(), "value", k)).collect(Collectors.toList()));

    }

    /**
     * 获取好友列表
     *
     * @param botUid 机器人UID
     */
    public AjaxResult getFriendList(Long botUid) {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        try {
            return container.robots.containsKey(botUid)
                    ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getFriendList().getData().stream().map(f -> Map.of("label", f.getNickname(), "value", f.getUserId())).collect(Collectors.toList()))
                    : new AjaxResult(HttpCodeEnum.ERROR, "此机器人未链接", null);
        } catch (Exception e) {
            return new AjaxResult(HttpCodeEnum.ERROR, "获取好友列表失败,请手动输入管理员账号！");
        }
    }

    /**
     * 获取群列表
     *
     * @param botUid 机器人UID
     */
    public AjaxResult getGroupList(Long botUid) {
        if (container.robots.isEmpty()) return AjaxResult.error(I18nUtils.message("request.error.bot.not.container"));
        try {
            return container.robots.containsKey(botUid)
                    ? new AjaxResult(HttpCodeEnum.SUCCESS, "", container.robots.get(botUid).getGroupList().getData().stream().map(f -> Map.of("label", f.getGroupName(), "value", f.getGroupId())).collect(Collectors.toList()))
                    : new AjaxResult(HttpCodeEnum.ERROR, "此机器人未链接", null);
        }catch (Exception e){
            return new AjaxResult(HttpCodeEnum.ERROR,"获取群列表失败，请手动输入群账号！");
        }
    }

    /**
     * 黑白名单过滤, 白名单优先级高于黑名单
     * 白名单中存在时，返回true
     * 白名单中不存在时，黑名单中存在，则返回false
     * 默认返回 true
     *
     * @param group group
     * @param prove prove
     * @return 是否存在名单中
     */
    public boolean isCheck(Long group, Long prove) {
        // 白名单中存在时，返回true
        if (ws.isWhite(group, prove)) return true;
        // 黑名单中存在，则返回false
        if (!bs.isBlack(group, prove)) return false;
        // 默认返回 true
        return true;
    }
}
