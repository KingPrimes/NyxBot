package com.nyx.bot.plugin.warframe.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

@Slf4j
public class TestMission {

    @Test
    public void testArbitration() {
        //log.info(JSON.toJSONString(ApiUrl.arbitrationPre()));
        SecretKey key = Jwts.SIG.HS256.key().build();
        String secret = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(secret);

    }
}
