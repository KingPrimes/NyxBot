package com.nyx.bot.controller.data.ai;

import com.nyx.bot.core.controller.BaseController;
import com.nyx.bot.core.page.TableDataInfo;
import com.nyx.bot.entity.bot.IssueReply;
import com.nyx.bot.repo.impl.IssueReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/data/ai/thesaurus")
public class IssueReplyController extends BaseController {

    @Autowired
    IssueReplyService service;

    String prefix = "data/ai/";

    @GetMapping
    public String thesaurus() {
        return prefix + "thesaurus";
    }


    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(IssueReply ir) {
        Page<IssueReply> list = service.list(ir);
        return getDataTable(list.getContent(), list.getTotalElements());

    }

}
