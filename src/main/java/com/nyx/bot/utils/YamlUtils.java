package com.nyx.bot.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 对象与yaml字符串互转工具：测试完成，时间：2021年08月06日 14时08分18秒
 */
@Slf4j
public class YamlUtils {

    private final static DumperOptions OPTIONS = new DumperOptions();

    private static File file;

    private static InputStream ymlInputSteam;

    private static Object CONFIG_MAP;

    private static Yaml yaml;

    static {
        //将默认读取的方式设置为块状读取
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    /**
     * 使用其他方法之前必须调用一次 设置yml的输出文件,当没有设置输入流时可以不设置输入流,默认以此文件读入
     *
     * @param file 输出的文件
     */
    public static void setYmlFile(File file) throws FileNotFoundException {
        YamlUtils.file = file;
        if (ymlInputSteam == null) {
            setYmlInputSteam(new FileInputStream(file));
        }
    }


    /**
     * 使用其他方法之前必须调用一次 设置yml的输入流
     *
     * @param inputSteam 输入流
     */
    public static void setYmlInputSteam(InputStream inputSteam) {
        ymlInputSteam = inputSteam;
        yaml = new Yaml(OPTIONS);
        CONFIG_MAP = yaml.load(ymlInputSteam);
    }

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 查询到的值
     */
    @SuppressWarnings("unchecked")
    public static Object getByKey(String key) {
        if (ymlInputSteam == null) {
            return null;
        }
        String[] keys = key.split("\\.");
        Object configMap = CONFIG_MAP;
        for (String s : keys) {
            if (configMap instanceof Map) {
                configMap = ((Map<String, Object>) configMap).get(s);
            } else {
                break;
            }
        }
        return configMap == null ? "" : configMap;
    }

    public static void saveOrUpdateByKey(String key, Object value) throws IOException {
        KeyAndMap keyAndMap = new KeyAndMap(key).invoke();
        key = keyAndMap.getKey();
        Map<String, Object> map = keyAndMap.getMap();
        map.put(key, value);
        //将数据重新写回文件
        yaml.dump(CONFIG_MAP, new FileWriter(file));
    }

    public static void removeByKey(String key) throws Exception {
        KeyAndMap keyAndMap = new KeyAndMap(key).invoke();
        key = keyAndMap.getKey();
        Map<String, Object> map = keyAndMap.getMap();
        Map<String, Object> fatherMap = keyAndMap.getFatherMap();
        map.remove(key);
        if (map.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = fatherMap.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                if (entry.getValue() == map) {
                    fatherMap.remove(entry.getKey());
                }
            }
        }
        yaml.dump(CONFIG_MAP, new FileWriter(file));
    }

    private static class KeyAndMap {
        private String key;
        private Map<String, Object> map;
        private Map<String, Object> fatherMap;

        public KeyAndMap(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public Map<String, Object> getMap() {
            return map;
        }

        public Map<String, Object> getFatherMap() {
            return fatherMap;
        }

        @SuppressWarnings("unchecked")
        public KeyAndMap invoke() {
            if (file == null) {
                System.err.println("请设置文件路径");
            }
            if (null == CONFIG_MAP) {
                CONFIG_MAP = new LinkedHashMap<>();
            }
            String[] keys = key.split("\\.");
            key = keys[keys.length - 1];
            map = (Map<String, Object>) CONFIG_MAP;
            for (int i = 0; i < keys.length - 1; i++) {
                String s = keys[i];
                if (map.get(s) == null || !(map.get(s) instanceof Map)) {
                    map.put(s, new HashMap<>(4));
                }
                fatherMap = map;
                map = (Map<String, Object>) map.get(s);
            }
            return this;
        }
    }


    /**
     * 将yaml字符串转成类对象
     *
     * @param yamlStr 字符串
     * @param clazz   目标类
     * @param <T>     泛型
     * @return 目标类
     */
    public static <T> T toObject(String yamlStr, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            return mapper.readValue(yamlStr, clazz);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 将类对象转yaml字符串
     *
     * @param object 对象
     * @return yaml字符串
     */
    public static String toYaml(Object object) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        StringWriter stringWriter = new StringWriter();
        try {
            mapper.writeValue(stringWriter, object);
            return stringWriter.toString();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return "";
    }

    /**
     * （此方法非必要）
     * json 2 yaml
     *
     * @param jsonStr json
     * @return yaml
     * @throws JsonProcessingException Exception
     */
    public static String json2Yaml(String jsonStr) throws JsonProcessingException {
        JsonNode jsonNode = new ObjectMapper().readTree(jsonStr);
        return new YAMLMapper().writeValueAsString(jsonNode);
    }

    /**
     * 读取Yaml文件到字符串
     *
     * @param filePath 文件路径
     * @return 返回的字符串
     */
    public static String readYamlFile(String filePath) {
        Yaml yaml = new Yaml();
        InputStream input;
        try {
            input = new FileInputStream(filePath);
            return yaml.dumpAsMap(yaml.load(input));
        } catch (Exception e) {
            log.error(e.getMessage());
            return "";
        }
    }
}

