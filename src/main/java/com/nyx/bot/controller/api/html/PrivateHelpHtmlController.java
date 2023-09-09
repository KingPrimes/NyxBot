package com.nyx.bot.controller.api.html;

import com.nyx.bot.entity.Services;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.utils.CacheUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/private")
public class PrivateHelpHtmlController {

    @GetMapping("/getHelpHtml/{permission}")
    public String getHtml(@PathVariable("permission") PermissionsEnums pe, Model model) {
        Map<String, String> v = new HashMap<>();
        switch (pe) {
            case USER -> {
                for (Codes value : isOpen()) {
                    if (value.getPermissions().equals(PermissionsEnums.USER)) {
                        v.put(value.getStr(), value.getPermissions().getStr());
                    }
                }
            }
            case ADMIN -> {
                for (Codes value : isOpen()) {
                    if (value.getPermissions().equals(PermissionsEnums.ADMIN) || value.getPermissions().equals(PermissionsEnums.USER)) {
                        v.put(value.getStr(), value.getPermissions().getStr());
                    }
                }
            }
            case SUPER_ADMIN -> {
                for (Codes value : isOpen()) {
                    v.put(value.getStr(), value.getPermissions().getStr());
                }
            }
        }
        model.addAttribute("codes", v);
        return "html/help";
    }

    private List<Codes> isOpen() {
        ArrayList servers = CacheUtils.get(CacheUtils.SYSTEM, "service", ArrayList.class);
        List<Codes> values = List.of(Codes.values());
        for (Object server : servers) {
            if (server instanceof Services) {
                if (!((Services) server).getSwit()) {
                    switch (((Services) server).getService()) {
                        case WARFRAME -> {
                            values = values.stream().filter(c -> !c.name().contains("WARFRAME")).collect(Collectors.toList());
                        }
                        case MUSIC -> {
                            values = values.stream().filter(c -> !c.name().contains("MUSIC")).collect(Collectors.toList());
                        }
                        case LOOK_IMAGE -> {
                            values = values.stream().filter(c -> !c.name().contains("IMAGE_NSFW")).collect(Collectors.toList());
                        }
                        case DRAW_EMOJIS -> {
                            values = values.stream().filter(c -> !c.name().contains("EXPRESSION")).collect(Collectors.toList());
                        }
                        case CHAT_GPT -> {
                            values = values.stream().filter(c -> !c.name().contains("CHAT_GPT")).collect(Collectors.toList());
                        }
                        case EPIC_GAMES -> {

                        }
                        case STABLE_DIFFUSION -> {
                            values = values.stream().filter(c -> !c.name().contains("DRAWING")).collect(Collectors.toList());
                        }
                    }
                }
            }
        }
        return values;
    }
}
