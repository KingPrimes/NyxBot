package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.AjaxResult;
import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/data/warframe/subscribe")
public class MissionSubscribeController extends BaseController {

    @Resource
    MissionSubscribeRepository repository;


    @GetMapping
    public AjaxResult subscribe() {
        return success().put("sub", SubscribeEnums.values());
    }

    @GetMapping("/detail/{subGroup}")
    public AjaxResult detail(@PathVariable Long subGroup) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        return success().put("group", group);
    }

    @GetMapping("/edit/{subGroup}")
    public AjaxResult edit(@PathVariable Long subGroup) {
        MissionSubscribe group = repository.findByGroupId(subGroup);
        return success().put("group", group);
    }

    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody MissionSubscribe ms) {

        return getDataTable(
                repository.findAllPageable(
                        ms.getSubGroup(),
                        PageRequest.of(
                                ms.getPageNum() - 1,
                                ms.getPageSize()
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
