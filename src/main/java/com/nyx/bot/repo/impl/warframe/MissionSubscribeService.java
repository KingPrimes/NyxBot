package com.nyx.bot.repo.impl.warframe;

import com.nyx.bot.enums.SubscribeEnums;
import org.springframework.stereotype.Service;

@Service("mss")
public class MissionSubscribeService {

    public String getStr(String str) {
        return SubscribeEnums.valueOf(str).getNAME();
    }
}
