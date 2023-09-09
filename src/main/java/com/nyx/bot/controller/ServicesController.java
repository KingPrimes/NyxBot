package com.nyx.bot.controller;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.ApiUrl;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.data.WarframeDataSource;
import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.ServicesEnums;
import com.nyx.bot.plugin.warframe.utils.WarframeSocket;
import com.nyx.bot.repo.ServicesRepository;
import com.nyx.bot.repo.impl.sys.SysMenuService;
import com.nyx.bot.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/services")
public class ServicesController extends BaseController {

    @Autowired
    ServicesRepository repository;

    @Autowired
    SysMenuService menu;

    @GetMapping
    public String services(ModelMap map) {
        map.put("services", repository.findAll());
        return "services";
    }


    @PostMapping
    @ResponseBody
    public AjaxResult switchService(Long id, String name, Boolean check) {
        Services services = new Services();
        services.setId(id);
        services.setService(ServicesEnums.valueOf(name));
        services.setSwit(check);
        ServicesEnums enums = ServicesEnums.valueOf(name);
        switch (enums) {
            case ICQQ_ONEBOTS -> {
                Boolean swit = repository.findByService(ServicesEnums.GO_CQHTTP).getSwit();
                if (swit) {
                    return error("请关闭GoCqHttp服务");
                }
                if (check) {
                    menu.openIcqq();
                } else {
                    menu.offIcqq();
                }
            }

            case GO_CQHTTP -> {
                Boolean swit = repository.findByService(ServicesEnums.ICQQ_ONEBOTS).getSwit();
                if (swit) {
                    return error("请关闭ICQQ服务");
                }
                if (check) {
                    menu.openGocq();
                } else {
                    menu.offGocq();
                }
            }
            case WARFRAME -> {
                if (check) {
                    //进行数据初始化
                    WarframeDataSource.init();
                    //链接任务 SOCKET
                    WarframeSocket.socket().connectServer(ApiUrl.WARFRAME_SOCKET);
                    //显示Warframe菜单选项
                    menu.openWarframe();
                } else {
                    //关闭任务 SOCKET
                    WarframeSocket.socket().close();
                    //移除Warframe菜单选项
                    menu.offWarframe();
                }
            }
            case CHAT_GPT -> {
                if (check) {
                    menu.openChatGpt();
                } else {
                    menu.offChatGpt();
                }
            }

            case STABLE_DIFFUSION -> {
                if (check) {
                    menu.openStableDiffusion();
                } else {
                    menu.offStableDiffusion();
                }
            }

            case YI_YAN -> {
                if (check) {
                    menu.openYiYan();
                } else {
                    menu.offYiYan();
                }
            }
        }
        repository.save(services);
        //更新缓存
        CacheUtils.set(CacheUtils.SYSTEM, "service", repository.findAll());
        return success();
    }

}
