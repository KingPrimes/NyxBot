package com.nyx.bot.controller.data.warframe;

import com.fasterxml.jackson.annotation.JsonView;
import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.Views;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/data/warframe/subscribe")
public class MissionSubscribeController extends BaseController {

    @Resource
    MissionSubscribeRepository repository;


    @GetMapping
    public AjaxResult subscribe() {
        return success().put("data", Map.of("sub", Arrays.stream(SubscribeEnums.values()).collect(Collectors.toMap(SubscribeEnums::name, SubscribeEnums::getNAME))));
    }

    @GetMapping("/detail/{subGroup}")
    public AjaxResult detail(@PathVariable Long subGroup) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        return success().put("data", Map.of("group", group));
    }

    @GetMapping("/edit/{subGroup}")
    public AjaxResult edit(@PathVariable Long subGroup) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        return success().put("data", Map.of("group", group));
    }

    @PostMapping("/list")
    @JsonView(Views.View.class)
    public TableDataInfo list(@RequestBody MissionSubscribe ms) {

        return getDataTable(
                repository.findAllPageable(
                        ms.getSubGroup(),
                        PageRequest.of(
                                ms.getCurrent() - 1,
                                ms.getSize()
                        )
                ).map(subscribe -> {
                    subscribe.setSubUsers(
                            subscribe.getSubUsers().stream()
                                    .peek(s ->
                                            s.setTypeList(
                                                    s.getTypeList()
                                                            .stream()
                                                            .peek(t -> t.setSubscribeType(t.getSubscribe().getNAME()))
                                                            .toList()
                                            )
                                    )
                                    .toList()
                    );
                    return subscribe;
                })
        );
    }


}
