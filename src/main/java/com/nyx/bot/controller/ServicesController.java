package com.nyx.bot.controller;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.ServicesEnums;
import com.nyx.bot.repo.ServicesRepository;
import com.nyx.bot.repo.impl.sys.SysMenuService;
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
                if (check) menu.openIcqq();
                else menu.offIcqq();
            }

            case GO_CQHTTP -> {
                if (check) menu.openGocq();
                else menu.offGocq();
            }
            case WARFRAME -> {
                if (check) menu.openWarframe();
                else menu.offWarframe();
            }
            case CHAT_GPT -> {
                if (check) menu.openChatGpt();
                else menu.offChatGpt();
            }

            case STABLE_DIFFUSION -> {
                if (check) menu.openStableDiffusion();
                else menu.offStableDiffusion();
            }

            case YI_YAN -> {
                if (check) menu.openYiYan();
                else menu.offYiYan();
            }
        }
        repository.save(services);
        return success();
    }

}
