package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.nyx.bot.res.GlobalStates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.nyx.bot.plugin.warframe.utils.GlobalStatesUtils.takeTheDifferenceSet;

public class TestSubscribe {

    @Test
    void testSubscribe() {
        String old = """
                [{"activation":"2025-02-11 20:23:02","active":true,"enemy":"Crossfire","enemyKey":"Crossfire","eta":"4m 43s","expired":false,"expiry":"2025-02-11 22:06:43","id":"67ab4126a42a4209dec1c18d","isHard":true,"isStorm":false,"missionKey":"Void Cascade","missionType":"Void Cascade","node":"Tuvul Commons (Zariman)","nodeKey":"Tuvul Commons (Zariman)","startString":"-1h 38m 57s","tier":"Omnia","tierNum":6},{"activation":"2025-02-11 19:40:01","active":true,"enemy":"Corpus","enemyKey":"Corpus","eta":"58m 11s","expired":false,"expiry":"2025-02-11 21:10:01","id":"67ab2902ea3df5b337c1c191","isHard":false,"isStorm":true,"missionKey":"Survival","missionType":"Survival","node":"Lu-yan (Veil)","nodeKey":"Lu-yan (Veil)","startString":"-31m 48s","tier":"Axi","tierNum":4}]
                                
                """;
        String now = """
                [{"activation":"2025-02-11 20:23:02","active":true,"enemy":"Crossfire","enemyKey":"Crossfire","eta":"3m 43s","expired":false,"expiry":"2025-02-11 22:06:43","id":"67ab4126a42a4209dec1c18d","isHard":true,"isStorm":false,"missionKey":"Void Cascade","missionType":"Void Cascade","node":"Tuvul Commons (Zariman)","nodeKey":"Tuvul Commons (Zariman)","startString":"-1h 38m 57s","tier":"Omnia","tierNum":6},{"activation":"2025-02-11 21:40:01","active":true,"enemy":"Corpus","enemyKey":"Corpus","eta":"1h 8m 1s","expired":false,"expiry":"2025-02-11 23:10:01","id":"67ab45222add300fe6c1c190","isHard":false,"isStorm":true,"missionKey":"Orphix","missionType":"Orphix","node":"Peregrine Axis (Pluto)","nodeKey":"Peregrine Axis (Pluto)","startString":"-21m 58s","tier":"Axi","tierNum":4}]
                                
                """;
        List<GlobalStates.Fissures> oldGs = JSON.parseArray(old, GlobalStates.Fissures.class, JSONReader.Feature.SupportSmartMatch);
        List<GlobalStates.Fissures> nowGs = JSON.parseArray(now, GlobalStates.Fissures.class, JSONReader.Feature.SupportSmartMatch);
        List<GlobalStates.Fissures> fissures = takeTheDifferenceSet(oldGs, nowGs);
        System.out.println(JSON.toJSONString(fissures));

    }
}
