package com.nyx.bot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FileUtils {
    //读取文件到字符串
    public static String readFileToString(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }
        byte[] b = new byte[0];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int len = inputStream.available();
            b = new byte[len];
            inputStream.read(b);
            inputStream.close();
        } catch (Exception e) {
            log.error("read file error:{}", e.getMessage());
        }
        return new String(b, StandardCharsets.UTF_8);
    }

    /**
     * 写入文件
     *
     * @param fileName 文件路径+文件名字
     * @param content  要写入的内容
     */
    public static void writeFile(String fileName, String content) {
        File file = new File(fileName);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("write file error:{}", e.getMessage());
        }
    }

    /**
     * 写入文件
     *
     * @param fileName 文件路径+文件名字
     * @param content  要写入的内容
     */
    public static void writeFile(String fileName, byte[] content) {
        File file = new File(fileName);
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(content);
        } catch (Exception e) {
            log.error("write file error:{}", e.getMessage());
        }
    }

}
