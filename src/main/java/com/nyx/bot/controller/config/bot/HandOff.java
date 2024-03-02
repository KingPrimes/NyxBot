package com.nyx.bot.controller.config.bot;

import com.nyx.bot.core.AjaxResult;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
public class HandOff {

    File file = new File("./locate.yaml");

    public AjaxResult handoff() {
        Yaml yaml = new Yaml();
        FileWriter fileWriter;
        try {
            Map<String, Object> load = yaml.load(new FileInputStream(file));
            Boolean isBW = (Boolean) load.get("isBW");
            load.put("isBW", !isBW);
            fileWriter = new FileWriter(file);
            fileWriter.write(yaml.dumpAsMap(load));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write("isBW: true");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException ignored) {

            }
            return AjaxResult.error();
        }
        return AjaxResult.success();
    }

    public Boolean isBW() {
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> load = yaml.load(new FileInputStream(file));
            return (Boolean) load.get("isBW");
        } catch (Exception e) {
            handoff();
            return true;
        }
    }

}
