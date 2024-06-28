package com.nyx.bot.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceUtil {

    public String getArbitrations(){
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("Arbitration.json");
        if (inputStream == null) return null;
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }catch (Exception ignored){
        }
        return null;
    }
}
