package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Locale;

@SpringBootTest(classes = NyxBotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@Slf4j
public class TestCleanseData {
    @Test
    void testCleanseDataItem() throws FileNotFoundException {
        JSONArray items = JSON.parseArray(new FileInputStream("D:\\Demos\\NyxBot\\data\\phpData\\item.json"));
        items.stream().map(item -> ((JSONObject) item).getString("category"))
                .distinct()
                .map(this::toEnumFormat)
                .sorted()
                .toList().forEach(item -> System.out.println(item + ","));
    }

    private String toEnumFormat(String str) {
        if (str == null || str.isBlank()) return "";
        return str.chars()
                .mapToObj(c -> (char) c)
                .map(c -> Character.isSpaceChar(c) ? "_" : Character.isUpperCase(c) ? "_" + c : Character.toString(c))
                .reduce("", (a, b) -> a + b)
                .toUpperCase(Locale.ROOT)
                .replaceAll("^_", "")
                .replaceAll("__", "_");
    }

}
