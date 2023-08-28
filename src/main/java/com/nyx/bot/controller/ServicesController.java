package com.nyx.bot.controller;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.ServicesEnums;
import com.nyx.bot.repo.ServicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/services")
public class ServicesController extends BaseController {

    @Autowired
    ServicesRepository repository;

    @GetMapping
    public String services(ModelMap map){
        map.put("services",repository.findAll());
        return "services";
    }


    @PostMapping
    @ResponseBody
    public AjaxResult switchService(Long id,String name,Boolean check){
        Services services = new Services();
        services.setId(id);
        services.setService(ServicesEnums.valueOf(name));
        services.setSwit(check);
        Services s = repository.save(services);
        if(s!=null){
            return success();
        }
        return error();
    }

}
