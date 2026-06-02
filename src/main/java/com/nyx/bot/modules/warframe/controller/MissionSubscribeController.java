package com.nyx.bot.modules.warframe.controller;

import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.controller.BaseController;
import com.nyx.bot.common.core.page.PageData;
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Warframe 事件订阅组
 */
@RestController
@RequestMapping("/data/warframe/subscribe")
@Validated
public class MissionSubscribeController extends BaseController {


    private final SubscriptionApplicationService subscriptionService;

    public MissionSubscribeController(SubscriptionApplicationService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/sub")
    public ApiResponse<Object> subscribe() {
        return success(Arrays.stream(SubscribeEnums.values()).collect(Collectors.toMap(SubscribeEnums::name, SubscribeEnums::getNAME)));
    }

    @GetMapping("/type")
    public ApiResponse<Object> getTypeEnums() {
        return success(Arrays.stream(MissionTypeEnum.values()).collect(Collectors.toMap(MissionTypeEnum::name, MissionTypeEnum::getName)));
    }

    @PostMapping("/list")
    public ApiResponse<PageData<?>> list(@RequestBody MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<@NonNull MissionSubscribe> page = subscriptionService.findAllSubscriptions(ms.getSubGroup(), pageable);
        return getDataTable(page);
    }

    @PostMapping("/user/list")
    public ApiResponse<PageData<?>> userList(@RequestBody @Validated MissionSubscribe ms) {
        Pageable pageable = PageRequest.of(ms.getCurrent() - 1, ms.getSize());
        Page<@NonNull MissionSubscribeUser> page = subscriptionService.findAllUsersBySubId(ms.getId(), pageable);
        return getDataTable(page);
    }

    @PostMapping("/type/list")
    public ApiResponse<PageData<?>> typeList(@RequestBody @Validated MissionSubscribeUser user) {
        Pageable pageable = PageRequest.of(user.getCurrent() - 1, user.getSize());
        Page<@NonNull MissionSubscribeUserCheckType> page = subscriptionService.findAllCheckTypesByUserId(user.getId(), pageable);
        return getDataTable(page);
    }

    // 删除订阅组
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        subscriptionService.deleteSubscribeGroup(id);
        return success();
    }

    @DeleteMapping("/user/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        subscriptionService.deleteSubscribeUser(id);
        return success();
    }

    @DeleteMapping("/type/{id}")
    public ApiResponse<Void> deleteCheckType(@PathVariable Long id) {
        subscriptionService.deleteCheckType(id);
        return success();
    }


}
