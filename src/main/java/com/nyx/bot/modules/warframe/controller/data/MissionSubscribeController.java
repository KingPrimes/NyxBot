package com.nyx.bot.modules.warframe.controller.data;

import com.nyx.bot.common.core.AjaxResult;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.TableDataInfo;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.enums.WarframeMissionTypeEnum;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserCheckTypeRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeUserRepository;
import com.nyx.bot.modules.warframe.service.MissionSubscribeService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/data/warframe/subscribe")
@Validated
public class MissionSubscribeController extends BaseController {

    @Resource
    MissionSubscribeRepository repository;
    @Resource
    MissionSubscribeService mss;

    @Resource
    MissionSubscribeUserRepository msu;

    @Resource
    MissionSubscribeUserCheckTypeRepository msuct;

    @GetMapping("/sub")
    public AjaxResult subscribe() {
        return success().put("data", Arrays.stream(SubscribeEnums.values()).collect(Collectors.toMap(SubscribeEnums::name, SubscribeEnums::getNAME)));
    }

    @GetMapping("/type")
    public AjaxResult getTypeEnums(){
        return success().put("data", Arrays.stream(WarframeMissionTypeEnum.values()).collect(Collectors.toMap(WarframeMissionTypeEnum::name, WarframeMissionTypeEnum::get)));
    }

    @PostMapping("/list")
    public TableDataInfo list(@RequestBody MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<MissionSubscribe> page = repository.findAllPageable(ms.getSubGroup(), pageable);
        return getDataTable(page);
    }

    @PostMapping("/user/list")
    public TableDataInfo userList(@RequestBody @Validated(Validated.class) MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<MissionSubscribeUser> page = msu.findAllBySUB_ID(ms.getId(), pageable);
        return getDataTable(page);
    }

    @PostMapping("/type/list")
    public TableDataInfo typeList(@RequestBody @Validated(Validated.class) MissionSubscribeUser user) {
        Pageable pageable = PageRequest.of(user.getCurrent() - 1, user.getSize());
        Page<MissionSubscribeUserCheckType> page = msuct.findAllBySUBU_ID(user.getId(), pageable);
        return getDataTable(page);
    }

    // 删除订阅组
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        mss.deleteSubscribeGroup(id);
        return success();
    }

    @DeleteMapping("/user/{id}")
    public AjaxResult deleteUser(@PathVariable Long id) {
        mss.deleteSubscribeUser(id);
        return success();
    }

    @DeleteMapping("/type/{id}")
    public AjaxResult deleteCheckType(@PathVariable Long id) {
        mss.deleteCheckType(id);
        return success();
    }


}
