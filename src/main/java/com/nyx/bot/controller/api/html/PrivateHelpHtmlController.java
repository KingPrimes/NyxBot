package com.nyx.bot.controller.api.html;

import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.PermissionsEnums;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/private")
public class PrivateHelpHtmlController {

    @GetMapping("/getHelpHtml/{permission}")
    public String getHtml(@PathVariable("permission") PermissionsEnums pe, Model model) {
        Map<String, String> v = new HashMap<>();
        switch (pe) {
            case USER -> {
                for (Codes value : Codes.values()) {
                    if (value.getPermissions().equals(PermissionsEnums.USER)) {
                        v.put(value.getStr(), value.getPermissions().getStr());
                    }
                }
            }
            case ADMIN -> {
                for (Codes value : Codes.values()) {
                    if (value.getPermissions().equals(PermissionsEnums.ADMIN) || value.getPermissions().equals(PermissionsEnums.USER)) {
                        v.put(value.getStr(), value.getPermissions().getStr());
                    }
                }
            }
            case SUPER_ADMIN -> {
                for (Codes value : Codes.values()) {
                    v.put(value.getStr(), value.getPermissions().getStr());
                }
            }
        }
        model.addAttribute("codes", v);
        return "html/help";
    }
}
