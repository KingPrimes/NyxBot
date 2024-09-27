package com.nyx.bot.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ResourceUtil {

    public String getArbitrations(){
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("Arbitration.json");
        if (inputStream == null) return null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }catch (Exception ignored){
        }
        return null;
    }
}
