package com.nyx.bot.data;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nyx.bot.NyxBotApplication;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
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

    @Test
    void testGetZhTranslation() throws IOException {
        HttpUtils.Body body = HttpUtils.sendGet("https://content.warframe.com/PublicExport/Manifest/ExportManifest.json!00_QuYGmcTCltbR7qG1eNIvSQ");
        File file = new File("D:\\Demos\\NyxBot\\data\\phpData\\ExportManifest.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(body.body());
        }
    }

}
