package com.nyx.bot.controller.data.warframe;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.entity.warframe.RivenItems;
import com.nyx.bot.repo.warframe.RivenItemsRepository;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/warframe/market/riven")
public class MarketRivenController extends BaseController {
    String prefix = "data/warframe/";

    @Resource
    RivenItemsRepository repository;


    @GetMapping
    public String market() {
        return prefix + "riven";
    }

    @PostMapping("/list")
    @ResponseBody
    public ResponseEntity list(RivenItems rivenItems) {
        return getDataTable(
                repository.findAllPageable(
                        rivenItems.getItemName(),
                        rivenItems.getRivenType(),
                        PageRequest.of(
                                rivenItems.getPageNum() - 1,
                                rivenItems.getPageSize())
                )
        );
    }

}
